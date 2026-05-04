package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Notification;
import tn.esprit.pi.domain.NotificationType;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.repository.NotificationRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void notifyPostRejected(User user, String postContent) {
        String preview = postContent != null && postContent.length() > 40
                ? postContent.substring(0, 40) + "…"
                : postContent;
        Notification n = Notification.builder()
                .user(user)
                .type(NotificationType.POST_REJECTED)
                .message("Your post \"" + preview + "\" was removed by a moderator because it violates our community guidelines (inappropriate content).")
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    public void notifyCommentRejected(User user, String commentContent) {
        String preview = commentContent != null && commentContent.length() > 40
                ? commentContent.substring(0, 40) + "…"
                : commentContent;
        Notification n = Notification.builder()
                .user(user)
                .type(NotificationType.COMMENT_REJECTED)
                .message("Your comment \"" + preview + "\" was removed by a moderator because it violates our community guidelines (inappropriate content).")
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    public void notifyPostLiked(User postOwner, String likerUsername, Long postId) {
        // Don't notify if the user liked their own post
        if (postOwner == null) return;
        Notification n = Notification.builder()
                .user(postOwner)
                .type(NotificationType.POST_LIKED)
                .message(likerUsername + " liked your post")
                .postId(postId)
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    public void notifyPostCommented(User postOwner, String commenterUsername, Long postId) {
        if (postOwner == null) return;
        Notification n = Notification.builder()
                .user(postOwner)
                .type(NotificationType.POST_COMMENTED)
                .message(commenterUsername + " commented on your post")
                .postId(postId)
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    public void notifyCommentLiked(User commentOwner, String likerUsername, Long postId) {
        if (commentOwner == null) return;
        Notification n = Notification.builder()
                .user(commentOwner)
                .type(NotificationType.COMMENT_LIKED)
                .message(likerUsername + " liked your comment")
                .postId(postId)
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    public void notifyCommentReplied(User commentOwner, String replierUsername, Long postId) {
        if (commentOwner == null) return;
        Notification n = Notification.builder()
                .user(commentOwner)
                .type(NotificationType.COMMENT_REPLIED)
                .message(replierUsername + " replied to your comment")
                .postId(postId)
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public void markAllRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
