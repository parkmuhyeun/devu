package com.devu.backend.entity.post;

import com.devu.backend.entity.Image;
import com.devu.backend.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Set;

@DiscriminatorValue("Q")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends Post{
    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus;

    @Builder
    public Question(Long id, User user, String title, String content, Long hit, Long like, Set<Image> images, QuestionStatus qnaStatus) {
        super(id, user, title, content, hit, like, images);
        this.questionStatus = qnaStatus;
    }

    public void updateStatus(QuestionStatus questionStatus) {
        this.questionStatus = questionStatus;
    }
}