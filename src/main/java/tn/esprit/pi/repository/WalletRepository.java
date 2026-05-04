package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User user);

    @Query(value = "SELECT w.id, w.points, u.id as user_id, u.username, u.email, u.role, u.phone " +
            "FROM wallet w LEFT JOIN users u ON w.user_id = u.id",
            nativeQuery = true)
    List<Object[]> findAllWalletsRaw();

    /** Leaderboard: all wallets with user info, ordered by points descending */
    @Query(value = "SELECT u.username, u.email, w.points " +
                   "FROM wallet w JOIN users u ON w.user_id = u.id " +
                   "ORDER BY w.points DESC",
           nativeQuery = true)
    List<Object[]> findLeaderboard();
}