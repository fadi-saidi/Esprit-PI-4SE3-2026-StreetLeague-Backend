package tn.esprit.pi.service;
import tn.esprit.pi.domain.Like;

import java.util.List;
import java.util.Optional;
public interface ILikeService {

        Like createLike(Like like);

        List<Like> getAllLikes();

        Optional<Like> getLikeById(Long id);

        void deleteLike(Long id);

        List<Like> getLikesByPost(Long postId);

        List<Like> getLikesByUser(Long userId);

}
