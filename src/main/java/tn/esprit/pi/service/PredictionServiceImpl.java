package tn.esprit.pi.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.*;
import tn.esprit.pi.repository.*;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements IPredictionService {

    private final PredictionRepository predictionRepository;
    private final PlayerStatRepository playerStatRepository;
    private final VirtualTeamRepository virtualTeamRepository;
    private final WalletService walletService;
    private final MatchRepository matchRepository;
    private final WalletRepository walletRepository;

    // ── Submit / update prediction for current week ───────────────────────────
    @Override
    public PredictionResponse submitPrediction(PredictionDto request) {
        int[] ww = currentWeek();
        int weekNumber = ww[0];
        int weekYear   = ww[1];

        VirtualTeam team = virtualTeamRepository.findById(request.getVirtualTeamId())
                .orElseThrow(() -> new RuntimeException("VirtualTeam not found"));

        // Reuse only if a PENDING prediction exists for this week (user editing before resolution).
        // If the existing prediction is RESOLVED, always create a fresh record so history is preserved.
        Prediction prediction = predictionRepository
                .findByVirtualTeamIdAndWeekNumberAndWeekYearAndStatus(
                        team.getId(), weekNumber, weekYear, PredictionStatus.PENDING)
                .orElse(Prediction.builder()
                        .virtualTeam(team)
                        .weekNumber(weekNumber)
                        .weekYear(weekYear)
                        .createdAt(LocalDate.now())
                        .status(PredictionStatus.PENDING)
                        .totalPointsEarned(0.0)
                        .playerPredictions(new ArrayList<>())
                        .build());

        prediction.setTotalPointsEarned(0.0);
        prediction.setCreatedAt(LocalDate.now());
        prediction.setCaptainPlayerId(request.getCaptainPlayerId());
        prediction.getPlayerPredictions().clear();

        for (PlayerPredictionEntryDto entry : request.getPlayers()) {
            PlayerPrediction pp = PlayerPrediction.builder()
                    .prediction(prediction)
                    .playerId(entry.getPlayerId())
                    .playerName(entry.getPlayerName())
                    .playerPosition(entry.getPlayerPosition())
                    .playerRating(entry.getPlayerRating())
                    .isCaptain(entry.getPlayerId().equals(request.getCaptainPlayerId()))
                    .predictGoal(entry.isPredictGoal())
                    .result(PlayerPredictionResult.PENDING)
                    .pointsEarned(0.0)
                    .build();
            prediction.getPlayerPredictions().add(pp);
        }

        return toResponse(predictionRepository.save(prediction));
    }

    // ── Get current week prediction for a team (latest record) ──────────────────
    @Override
    public Optional<PredictionResponse> getCurrentPrediction(Long virtualTeamId) {
        int[] ww = currentWeek();
        return predictionRepository
                .findFirstByVirtualTeamIdAndWeekNumberAndWeekYearOrderByIdDesc(virtualTeamId, ww[0], ww[1])
                .map(this::toResponse);
    }

    // ── Get all predictions history for a team ────────────────────────────────
    @Override
    public List<PredictionResponse> getPredictionHistory(Long virtualTeamId) {
        return predictionRepository
                .findByVirtualTeamIdOrderByWeekYearDescWeekNumberDesc(virtualTeamId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Admin: save player stats ──────────────────────────────────────────────
    @Override
    public void savePlayerStat(PlayerStatDto dto) {
        PlayerStat stat = playerStatRepository
                .findByPlayerIdAndWeekNumberAndWeekYear(
                        dto.getPlayerId(), dto.getWeekNumber(), dto.getWeekYear())
                .orElse(new PlayerStat());

        stat.setPlayerId(dto.getPlayerId());
        stat.setPlayerName(dto.getPlayerName());
        stat.setSportType(SportType.valueOf(dto.getSportType()));
        stat.setWeekNumber(dto.getWeekNumber());
        stat.setWeekYear(dto.getWeekYear());
        stat.setGoalsScored(dto.getGoalsScored());
        stat.setYellowCards(dto.getYellowCards());
        stat.setRedCards(dto.getRedCards());
        stat.setBasketballPoints(dto.getBasketballPoints());
        stat.setTennisWin(dto.isTennisWin());

        playerStatRepository.save(stat);
    }

    // ── Scheduler: every Monday 00:01 → resolve previous week ────────────────
    @Transactional
    @Override
    //@Scheduled(cron = "0 1 0 * * MON")
    @Scheduled(cron = "0 */2 * * * *")
    public void resolveLastWeekPredictions() {
        //LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        LocalDate lastWeek = LocalDate.now();
        WeekFields wf = WeekFields.of(Locale.getDefault());
        int weekNumber = lastWeek.get(wf.weekOfWeekBasedYear());
        int weekYear   = lastWeek.getYear();

        // ── Step 1: Auto-sync FINISHED match results → PlayerStat ────────────
        // Reads MatchPlayerStat records from every FINISHED match this week
        // and upserts them into the PlayerStat table so predictions can be resolved.
        syncMatchStatsToPlayerStats(weekNumber, weekYear);

        // ── Step 2: Resolve all PENDING predictions for this week ─────────────
        List<Prediction> pending = predictionRepository
                .findByStatusAndWeekNumberAndWeekYear(PredictionStatus.PENDING, weekNumber, weekYear);

        for (Prediction prediction : pending) {
            resolveOnePrediction(prediction);
        }
    }

    // ── Sync FINISHED match results into PlayerStat table ─────────────────────
    // Called by the scheduler before resolving predictions.
    // For each FINISHED match in the week, upserts a PlayerStat row per player.
    private void syncMatchStatsToPlayerStats(int weekNumber, int weekYear) {
        List<Match> finishedMatches = matchRepository
                .findByStatusAndWeekNumberAndWeekYear(EventStatus.FINISHED, weekNumber, weekYear);

        for (Match match : finishedMatches) {
            if (match.getPlayerStats() == null || match.getPlayerStats().isEmpty()) continue;

            for (MatchPlayerStat mps : match.getPlayerStats()) {
                if (mps.getPlayerId() == null) continue;

                // Upsert: if a stat already exists for this player+week, update it.
                // This handles re-entered match results gracefully.
                PlayerStat stat = playerStatRepository
                        .findByPlayerIdAndWeekNumberAndWeekYear(mps.getPlayerId(), weekNumber, weekYear)
                        .orElse(new PlayerStat());

                stat.setPlayerId(mps.getPlayerId());
                stat.setPlayerName(mps.getPlayerName());
                stat.setSportType(mps.getSportType() != null
                        ? mps.getSportType()
                        : (match.getSportType() != null ? match.getSportType() : SportType.FOOTBALL));
                stat.setWeekNumber(weekNumber);
                stat.setWeekYear(weekYear);
                stat.setGoalsScored(mps.getGoalsScored());
                stat.setYellowCards(mps.getYellowCards());
                stat.setRedCards(mps.getRedCards());
                stat.setBasketballPoints(mps.getBasketballPoints());
                stat.setTennisWin(mps.isTennisWin());

                playerStatRepository.save(stat);
            }
        }
    }

    // ── Manually resolve by ID (admin endpoint) ───────────────────────────────
    @Override
    public PredictionResponse resolvePredictionById(Long predictionId) {
        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));
        resolveOnePrediction(prediction);
        return toResponse(predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("Prediction not found after resolve")));
    }

    // ── Admin: list all pending predictions ───────────────────────────────────
    @Override
    public List<PredictionResponse> getAllPendingPredictions() {
        return predictionRepository
                .findByStatusOrderByWeekYearDescWeekNumberDesc(tn.esprit.pi.domain.PredictionStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Admin: list all predictions (pending + resolved) ──────────────────────
    @Override
    public List<PredictionResponse> getAllPredictions() {
        return predictionRepository
                .findAllByOrderByWeekYearDescWeekNumberDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Resolve all pending predictions for a specific week (triggered by match result) ──
    @Override
    public List<PredictionResponse> resolveWeekPredictions(int weekNumber, int weekYear) {
        List<Prediction> pending = predictionRepository
                .findByStatusAndWeekNumberAndWeekYear(PredictionStatus.PENDING, weekNumber, weekYear);
        for (Prediction p : pending) {
            resolveOnePrediction(p);
        }
        return pending.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Core resolution logic ─────────────────────────────────────────────────
    private void resolveOnePrediction(Prediction prediction) {
        double total = 0.0;

        for (PlayerPrediction pp : prediction.getPlayerPredictions()) {
            Optional<PlayerStat> statOpt = playerStatRepository
                    .findByPlayerIdAndWeekNumberAndWeekYear(
                            pp.getPlayerId(),
                            prediction.getWeekNumber(),
                            prediction.getWeekYear());

            if (statOpt.isEmpty()) {
                pp.setResult(PlayerPredictionResult.PENDING);
                pp.setPointsEarned(0.0);
                continue;
            }

            PlayerStat stat  = statOpt.get();
            double points    = 0.0;
            String pos       = pp.getPlayerPosition();

            if (isTennis(pos)) {
                // Tennis: predict win → ×1.5 bonus if correct, -2 if wrong
                //         no predict → +rating/10 on win, 0 on loss (safe)
                boolean won = stat.isTennisWin();
                pp.setTennisWin(won);

                if (pp.isPredictGoal()) {
                    if (won) {
                        points = pp.getPlayerRating() / 10.0 * 1.5;
                        pp.setResult(PlayerPredictionResult.CORRECT);
                    } else {
                        points = -2.0;
                        pp.setResult(PlayerPredictionResult.WRONG);
                    }
                } else {
                    if (won) {
                        points = pp.getPlayerRating() / 10.0;
                        pp.setResult(PlayerPredictionResult.CORRECT);
                    } else {
                        pp.setResult(PlayerPredictionResult.WRONG);
                    }
                }

            } else if (isBasketball(pos)) {
                // Basketball: predict big game (30+ pts) → ×1.5 if correct, -2 if wrong
                //             no predict → normal scoring
                int bpts = stat.getBasketballPoints();
                pp.setBasketballPoints(bpts);

                if (pp.isPredictGoal()) {
                    if (bpts >= 30) {
                        points = pp.getPlayerRating() / 8.0 * 1.5;
                        pp.setResult(PlayerPredictionResult.CORRECT);
                    } else if (bpts >= 20) {
                        points = pp.getPlayerRating() / 10.0;
                        pp.setResult(PlayerPredictionResult.PARTIAL);
                    } else {
                        points = -2.0;
                        pp.setResult(PlayerPredictionResult.WRONG);
                    }
                } else {
                    if (bpts >= 30) {
                        points = pp.getPlayerRating() / 8.0;
                        pp.setResult(PlayerPredictionResult.CORRECT);
                    } else if (bpts >= 20) {
                        points = pp.getPlayerRating() / 10.0;
                        pp.setResult(PlayerPredictionResult.CORRECT);
                    } else {
                        pp.setResult(PlayerPredictionResult.WRONG);
                    }
                }

                if (pp.isCaptain()) points *= 2;

            } else {
                // Football
                int goals  = stat.getGoalsScored();
                int yellow = stat.getYellowCards();
                int red    = stat.getRedCards();

                pp.setGoalsScored(goals);
                pp.setYellowCards(yellow);
                pp.setRedCards(red);

                // Goal points depend on prediction
                if (pp.isPredictGoal()) {
                    if (goals > 0) {
                        // Correct prediction → 50% bonus
                        points += (pp.getPlayerRating() / 10.0) * goals * 1.5;
                        pp.setResult(PlayerPredictionResult.CORRECT);
                    } else {
                        // Predicted but didn't score → -2 penalty
                        points -= 2.0;
                        pp.setResult(PlayerPredictionResult.WRONG);
                    }
                } else {
                    if (goals > 0) {
                        // Didn't predict but scored → normal points, PARTIAL
                        points += (pp.getPlayerRating() / 10.0) * goals;
                        pp.setResult(PlayerPredictionResult.PARTIAL);
                    } else {
                        // Safe: correctly predicted no goal
                        pp.setResult(PlayerPredictionResult.CORRECT);
                    }
                }

                // Cards always deduct
                points -= yellow * 3.0;
                points -= red    * 6.0;

                // Override result when carded + goals
                if (goals > 0 && (yellow > 0 || red > 0)) {
                    pp.setResult(PlayerPredictionResult.PARTIAL);
                }

                if (pp.isCaptain()) points *= 2;
            }

            pp.setPointsEarned(points);
            total += points;
        }

        prediction.setTotalPointsEarned(total);
        prediction.setStatus(PredictionStatus.RESOLVED);
        predictionRepository.save(prediction);

        // Update team points
        VirtualTeam team = prediction.getVirtualTeam();
        team.setWeekPoints(team.getWeekPoints() + total);
        team.setEarnedPoints(team.getEarnedPoints() + total);
        virtualTeamRepository.save(team);

        // Credit earned points to team owner's wallet
        walletService.creditFantasyPoints(team.getUser(), (int) Math.round(total));
    }

    // ── Admin stats ───────────────────────────────────────────────────────────
    @Override
    public FantasyStatsDto getAdminStats() {
        int[] ww = currentWeek();
        int weekNumber = ww[0];
        int weekYear   = ww[1];

        // Total points distributed this week
        Double total = predictionRepository.sumPointsThisWeek(weekNumber, weekYear);

        // Most predicted player
        String mostPredicted = predictionRepository.mostPredictedPlayerName();

        // Best prediction this week → need username from virtualTeam.user
        FantasyStatsDto.BestPrediction best = null;
        List<Object[]> top = predictionRepository.topPredictionThisWeek(weekNumber, weekYear);
        if (!top.isEmpty()) {
            Object[] row = top.get(0);
            Long teamId = ((Number) row[0]).longValue();
            double pts  = ((Number) row[1]).doubleValue();
            String username = virtualTeamRepository.findById(teamId)
                    .map(t -> t.getUser() != null ? t.getUser().getUsername() : "Unknown")
                    .orElse("Unknown");
            best = new FantasyStatsDto.BestPrediction(username, pts);
        }

        int activeTeams = (int) virtualTeamRepository.count();

        return FantasyStatsDto.builder()
                .totalPointsThisWeek(total != null ? total : 0.0)
                .mostPredictedPlayer(mostPredicted != null ? mostPredicted : "—")
                .bestPrediction(best)
                .activeTeams(activeTeams)
                .build();
    }

    // ── Leaderboard ───────────────────────────────────────────────────────────
    @Override
    public List<LeaderboardEntryDto> getLeaderboard() {
        List<Object[]> rows = walletRepository.findLeaderboard();
        List<LeaderboardEntryDto> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Object[] r = rows.get(i);
            result.add(LeaderboardEntryDto.builder()
                    .rank(i + 1)
                    .username(r[0] != null ? r[0].toString() : "—")
                    .email(r[1] != null ? r[1].toString() : "—")
                    .walletPoints(((Number) r[2]).intValue())
                    .build());
        }
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean isTennis(String pos) {
        return "P1".equals(pos);
    }

    private boolean isBasketball(String pos) {
        return pos != null && (pos.equals("PG") || pos.equals("SG") ||
                pos.equals("SF") || pos.equals("PF") || pos.equals("C"));
    }

    private int[] currentWeek() {
        LocalDate now = LocalDate.now();
        WeekFields wf = WeekFields.of(Locale.getDefault());
        return new int[]{ now.get(wf.weekOfWeekBasedYear()), now.getYear() };
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private PredictionResponse toResponse(Prediction p) {
        List<PlayerPredictionResponse> ppList = p.getPlayerPredictions().stream()
                .map(pp -> PlayerPredictionResponse.builder()
                        .id(pp.getId())
                        .playerId(pp.getPlayerId())
                        .playerName(pp.getPlayerName())
                        .playerPosition(pp.getPlayerPosition())
                        .playerRating(pp.getPlayerRating())
                        .isCaptain(pp.isCaptain())
                        .predictGoal(pp.isPredictGoal())
                        .goalsScored(pp.getGoalsScored())
                        .yellowCards(pp.getYellowCards())
                        .redCards(pp.getRedCards())
                        .basketballPoints(pp.getBasketballPoints())
                        .tennisWin(pp.getTennisWin())
                        .result(pp.getResult())
                        .pointsEarned(pp.getPointsEarned())
                        .build())
                .collect(Collectors.toList());

        return PredictionResponse.builder()
                .id(p.getId())
                .virtualTeamId(p.getVirtualTeam().getId())
                .weekNumber(p.getWeekNumber())
                .weekYear(p.getWeekYear())
                .captainPlayerId(p.getCaptainPlayerId())
                .status(p.getStatus())
                .totalPointsEarned(p.getTotalPointsEarned())
                .createdAt(p.getCreatedAt())
                .playerPredictions(ppList)
                .build();
    }
}