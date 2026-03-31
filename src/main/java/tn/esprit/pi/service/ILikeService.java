package tn.esprit.pi.service;
import tn.esprit.pi.domain.Like;
import tn.esprit.pi.domain.LikeType;

import java.util.List;
import java.util.Optional;
public interface ILikeService {
        Like createLike(Long postId, Long userId, LikeType likeType);


        List<Like> getAllLikes();

        Optional<Like> getLikeById(Long id);

        void deleteLike(Long id);

        List<Like> getLikesByPost(Long postId);

        List<Like> getLikesByUser(Long userId);

}
