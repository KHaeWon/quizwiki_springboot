package com.multi.quizwiki.qboard.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.Alias;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Alias("qboard")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QboardDTO {
	private Long qboard_id;
	private String member_id;
	private String category;
	private String subject;
	private String title; // //게시물 제목
	private String content; //게시물내용 text
	private LocalDateTime regDate; // 게시물등록시간 datetime
	private LocalDateTime editDate;
	int commentCount;
	int likeCount;
	int view_Count;
	private Boolean delete_Yn;              // 삭제 여부
    private List<MultipartFile> files = new ArrayList<>();    // 첨부파일 List
    
	////not-column
    private String previewText;

	public QboardDTO(String category, String subject, String title, String content) {
		this.category = category;
		this.subject = subject;
		this.title = title;
		this.content = content;
	}
    
    
}
	