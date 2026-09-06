package com.example.portfolio.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 프로젝트의 주요 화면 이미지 한 장.
 * image 는 static 아래 경로이므로, 실제 스크린샷을 같은 경로에 덮어쓰면 그대로 교체된다.
 */
@Getter
@Setter
public class Shot {

    /** 이미지가 아직 없을 때 대신 보여줄 화면 */
    public static final String PLACEHOLDER_IMAGE = "/images/placeholder.svg";

    private String label;
    private String image;

    public static Shot placeholder(String label) {
        Shot shot = new Shot();
        shot.setLabel(label);
        shot.setImage(PLACEHOLDER_IMAGE);
        return shot;
    }

    /** 경로가 비어 있으면 자리표시자로 대체해 깨진 이미지가 노출되지 않게 한다 */
    public String getImage() {
        return (image == null || image.isBlank()) ? PLACEHOLDER_IMAGE : image;
    }

    public String getLabel() {
        return (label == null || label.isBlank()) ? "화면 준비 중" : label;
    }
}
