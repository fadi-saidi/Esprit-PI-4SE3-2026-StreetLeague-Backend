package tn.esprit.pi.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Like;
import tn.esprit.pi.repository.LikeRepository;
import tn.esprit.pi.service.ILikeService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements ILikeService {
    private final LikeRepository likeRepository;

    @Override
    public Like createLike(Like like) {
        return likeRepository.save(like);
    }

    @Override
    public List<Like> getAllLikes() {
        return likeRepository.findAll();
    }

    @Override
    public Optional<Like> getLikeById(Long id) {
        return likeRepository.findById(id);
    }

    @Override
    public void deleteLike(Long id) {
        likeRepository.deleteById(id);
    }

    @Override
    public List<Like> getLikesByPost(Long postId) {
        return likeRepository.findByPostId(postId);
    }

    @Override
    public List<Like> getLikesByUser(Long userId) {
        return likeRepository.findByUserId(userId);
    }

}
