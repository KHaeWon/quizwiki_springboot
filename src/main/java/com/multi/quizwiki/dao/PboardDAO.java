package com.multi.quizwiki.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.multi.quizwiki.entity.pboard.PboardCateEntity;
import com.multi.quizwiki.entity.pboard.PboardEntity;
import com.multi.quizwiki.entity.pboard.PboardReplyEntity;

public interface PboardDAO {
	public void insert(PboardEntity pboard);
	public void pboard_edit(PboardEntity pboard);
	public void reply_insert(PboardReplyEntity pboardReply);
	
	public Page<PboardEntity> pboard_findByCate(int pboardCate,Pageable pageable);
	public PboardEntity pboard_findByPboardId(String pboardId);
	
	public List<PboardCateEntity> pboardCate_findAll();
	
	
}
