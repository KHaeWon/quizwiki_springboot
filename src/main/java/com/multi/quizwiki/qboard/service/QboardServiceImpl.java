package com.multi.quizwiki.qboard.service;

import java.util.Collections;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.multi.quizwiki.qboard.dao.QboardDAO;
import com.multi.quizwiki.qboard.dto.QboardDTO;
import com.multi.quizwiki.qboard.dto.SearchDto;
import com.multi.quizwiki.qboard.paging.Pagination;
import com.multi.quizwiki.qboard.paging.PagingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QboardServiceImpl implements QboardService {
	QboardDAO qboarddao;
	
	@Autowired
	public QboardServiceImpl(QboardDAO qboarddao) {
		this.qboarddao =qboarddao;
	}
	
	//게시글 저장
	@Override
	public Long save(QboardDTO qboard) {
		log.info("insert service 실행" +qboard.getQboard_id());
		 qboarddao.save(qboard);
		 return qboard.getQboard_id();
	}

	@Override	
	public PagingResponse<QboardDTO> getBoardList(SearchDto params) {
		
		int count = qboarddao.count(params);
		if (count<1) {
			return new PagingResponse<>(Collections.emptyList(), null);
		}
		//pagination 객체를 생성해서 페이지 정보 계산 후 searchDto 타입의 객체인  params에 계산된 페이지 정보 저장
		Pagination pagination = new Pagination(count, params);
		params.setPagination(pagination);
		
        // 계산된 페이지 정보의 일부(limitStart, recordSize)를 기준으로 리스트 데이터 조회 후 응답 데이터 반환
		List<QboardDTO> list = qboarddao.getBoardList(params);
		return new PagingResponse<>(list, pagination);
	}

	@Override
	public QboardDTO getQboardDetail(Long qboard_id) {
		return qboarddao.getQboardDetail(qboard_id);

	}


	@Override
	public int update(QboardDTO qboard) {
		System.out.println(qboard);
		return qboarddao.update(qboard);
	}

	@Override
	public int deleteQboard(Long qboard_id) {
		log.info("서비스 실행");
		return qboarddao.deleteQboard(qboard_id);
	}

	@Override
	public int count(SearchDto params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void increaseViewCount(Long qboard_id) {
		 qboarddao.increaseViewCount(qboard_id);
	}

	@Override
	public List<QboardDTO> findByCategory(String category) {
		return qboarddao.findByCategory(category);
		
	}

	@Override
	public List<QboardDTO> findBySubject(String Subject) {
		
		return qboarddao.findBySubject(Subject);
	}
	

		
	
	
	/*
	 * public void save (QboardDTO qboard) { QboardEntity qboardEntitiy =
	 * QboardEntity.toSaveEntity(qboard); qboardRepo.save(qboardEntitiy); }
	 */
}
