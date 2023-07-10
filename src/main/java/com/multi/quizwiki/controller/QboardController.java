package com.multi.quizwiki.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.quizwiki.common.FileUploadLogicService;
import com.multi.quizwiki.dto.MemberDTO;
import com.multi.quizwiki.qboard.dto.FileRequest;
import com.multi.quizwiki.qboard.dto.QboardDTO;
import com.multi.quizwiki.qboard.dto.SearchDto;
import com.multi.quizwiki.qboard.paging.PagingResponse;
import com.multi.quizwiki.qboard.service.CommentService;
import com.multi.quizwiki.qboard.service.FileService;
import com.multi.quizwiki.qboard.service.LikeService;
import com.multi.quizwiki.qboard.service.QboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import util.FileUtils;
@Controller
@Slf4j
@RequiredArgsConstructor
public class QboardController {
	
	private static final String member_id = null;
	@Autowired
	QboardService qboardservice;
	@Autowired  
	private final FileService fileService;
	
	 private final FileUtils fileUtils;
	 private final CommentService commentservice;
	 private final LikeService likeservice;
	 @Autowired
	 private FileUploadLogicService fileUploadService;
	
	
	
	
	@RequestMapping("/qboard/list")
	public String show_qboard_list() {
		return "thymeleaf/qboard/editorTest";
	}
	
	@PostMapping("/qboard/save.do")
	@ResponseBody
	public String save(
			HttpServletRequest req,
			@RequestPart(name= "sendData") String sendData, 
			@RequestPart(name = "imageList", required = false) List<MultipartFile> imageList) throws IllegalStateException, IOException{
		
		MemberDTO loginUser = util.Utils.getSessionUser(req);
		if(loginUser == null) {
			return "notlogin";
		}
		
		String memberId = loginUser.getMember_id();
		
		ObjectMapper mapper = new ObjectMapper();
		//sendData를 java 객체로 변환?
		Map<String,String> map = mapper.readValue(sendData, Map.class);
		
		QboardDTO qboard = mapper.convertValue(map.get("qboard"), QboardDTO.class);
		
		qboard.setMember_id(memberId);
		List<FileRequest> fileReqList = new ArrayList<FileRequest>();
		if(imageList != null) {
			
			for(MultipartFile img : imageList) {
				String origin = img.getOriginalFilename();
				String uuid = fileUploadService.uploadFile(img, "qboardimage");
				
				fileReqList.add(new FileRequest(origin, uuid, img.getSize()));
				
				Document html = Jsoup.parse(qboard.getContent());
				Elements el = html.select("img[title="+origin+"]");
				
				el.attr("src", "/qboard/find/"+uuid);
				qboard.setContent(html.html());
			
				
			}
			
		}
		Long qboard_id = qboardservice.save(qboard);
		fileService.saveFiles(qboard_id, fileReqList);
		
		
		//Long qboard_id = qboardservice.save(qboard); //게시글 삽입 게시글은 db에 저장되는데 
		// 파일이 null 이 뜨는 듯
		//List<FileRequest> files = fileUtils.uploadFiles(qboard.getFiles()); //디스크에 파일 업로드
		//log.info("파일 컨트롤러 실행" +qboard.getFiles());
        //fileService.saveFiles(qboard_id, files); // 업로드 된 정보를 db에 저장
		return "success";
	}
	
	//업로드된 이미지를 찾아와서 뿌린당. 요거 어떻게 맵핑이랑 파일경로 잘 처리하면 통합적으로 만들수잇을거같긴한디 일단...
	@RequestMapping("/qboard/find/{uuid}")
	public ResponseEntity<UrlResource> qboard_find_image(@PathVariable String uuid) throws MalformedURLException{
		UrlResource resource = new UrlResource("file:"+fileUploadService.getUploadpath("qboardimage",uuid));
		
		return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_JPEG)
				.body(resource);
	}
	
	
	 // 쿼리 스트링 파라미터를 Map에 담아 반환
    private Map<String, Object> queryParamsToMap(final SearchDto queryParams) {
        Map<String, Object> data = new HashMap<>();
        data.put("page", queryParams.getPage());
        data.put("recordSize", queryParams.getRecordSize());
        data.put("pageSize", queryParams.getPageSize());
        data.put("keyword", queryParams.getKeyword());
        data.put("searchType", queryParams.getSearchType());
        return data;
    }

	
	@RequestMapping("/qboard/write.do")
	public String QboardWrite(@RequestParam(value="qboard_id", required = false) Long qboard_id, Model model, 
			HttpServletRequest req) {
		 if(!util.Utils.loginCheck(req)) {
			 return "redirect:/login.do";
		 }
		
		if(qboard_id != null) {
			 QboardDTO qboard = qboardservice.getQboardDetail(qboard_id);
			 model.addAttribute("qboard",qboard);
		}  
			 
		return "thymeleaf/qboard/qboard_write";
	}
	
	
	  @GetMapping("/qboard/list.do") 
	  public String QboardList(@ModelAttribute("params") SearchDto params, Model model, String category ) {
		  PagingResponse<QboardDTO> qboardlist = qboardservice.getBoardList(params);
		  List<QboardDTO> qboard =  qboardservice.findByCategory(category);	
		  
		  List<QboardDTO> qboardLists = qboardlist.getList();
			//PreviewHTML을 만드는 과정 (썸네일)
		  
		  qboardLists.forEach((q)->{
				String previewText = "";
				boolean isOverflow = false;
				Document html = Jsoup.parse(q.getContent());
				
				Elements pTags = html.select("p");
				for(Element pTag : pTags) {
					previewText += pTag.text();
					if(previewText.length() > 50) {
						isOverflow = true;
						break;
					}
					
				}
				previewText = previewText.substring(0, Math.min(previewText.length(), 50));
				if(isOverflow) {
					previewText += "···";
				}
				q.setPreviewText(previewText);
			});
		  qboardlist.setList(qboardLists);
		  
		  model.addAttribute("qbaord",qboard);
		  model.addAttribute("category",params.getCategory());
		  model.addAttribute("subject",params.getSubject());
		  model.addAttribute("qboardlist",qboardlist); 
		  
	  	return "thymeleaf/qboard/qboard_list"; 
	  }
	  
	  @GetMapping("/qboard/read.do" )
		 public String QboardDetail(@RequestParam(value="qboard_id",required = false) Long qboard_id, 
			 Model model, HttpSession session,HttpServletRequest req) {
		  
		  	QboardDTO qboard = qboardservice.getQboardDetail(qboard_id);
		  	qboardservice.increaseViewCount(qboard_id);
			 
			 
			 boolean isLike = false;
			 boolean isLogin = false;
			 
			 MemberDTO member = util.Utils.getSessionUser(req);
			 if(member!= null) {
				 isLogin = true;
				 isLike = likeservice.isLike(qboard_id, member.getMember_id());
			 }
			 
			 model.addAttribute("isLogin", isLogin);
			 model.addAttribute("qboard", qboard);
			 model.addAttribute("category", qboard.getCategory());
			 model.addAttribute("isLike", isLike);
			 model.addAttribute("likecount",likeservice.count(qboard_id));
			 
			return "thymeleaf/qboard/qboard_read";
		 }
		 	
	@GetMapping("/qboard/delete")
	public String deleteBoard(@RequestParam Long qboard_id) {
		qboardservice.deleteQboard(qboard_id);
		
		return "redirect:/qboard/list.do";
	}
	
	@PostMapping("/qboard/update.do")
	public String updateQboard(HttpServletRequest req,
			@RequestPart(name= "sendData") String sendData,
			@RequestPart(name = "imageList", required = false) List<MultipartFile> imageList
			) throws IllegalStateException, IOException {
		
		MemberDTO loginUser = util.Utils.getSessionUser(req);
		if(loginUser == null) {
			return "notlogin";
		}
		
		String memberId = loginUser.getMember_id();
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String,String> map = mapper.readValue(sendData, Map.class);
		
		QboardDTO qboard = mapper.convertValue(map.get("qboard"), QboardDTO.class);
		
		qboard.setMember_id(memberId);
		
		List<FileRequest> fileReqList = new ArrayList<FileRequest>();
		
		if(imageList != null) {
			
			for(MultipartFile img : imageList) {
				String origin = img.getOriginalFilename();
				String uuid = fileUploadService.uploadFile(img, "qboardimage");
				
				fileReqList.add(new FileRequest(origin, uuid, img.getSize()));
				
				Document html = Jsoup.parse(qboard.getContent());
				Elements el = html.select("img[title="+origin+"]");
				
				el.attr("src", "/qboard/find/"+uuid);
				qboard.setContent(html.html());
			}
		}
		
		fileService.saveFiles(qboard.getQboard_id(), fileReqList);
		
		qboardservice.update(qboard);
		
		return "true";
		
	}

	
	
		
	
 	@PostMapping("/qboard/upload/image")
 	@ResponseBody
 	public String img_upload(MultipartFile file) throws IllegalStateException, IOException {
 		String uuid = fileUploadService.uploadFile(file, "qboardimg");
 		return uuid;
 		
 	}
}