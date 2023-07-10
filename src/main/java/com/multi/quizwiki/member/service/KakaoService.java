package com.multi.quizwiki.member.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


// 코드설명 

//	1. getAccessToken: 사용자 인증 후 발급된 인증 코드를 사용하여 액세스 토큰(access token)을 얻습니다.
//	getUserInfo: 액세스 토큰을 이용하여 사용자 정보를 얻습니다.

//	2. getAccessToken 메서드 설명:
//	인자로 전달된 인증 코드를 사용하여 액세스 토큰을 받아옵니다.
//	HttpURLConnection을 사용하여 POST 요청을 보내고, 필요한 파라미터를 전송합니다.
//	JSON 형식의 응답을 받아와 Gson 라이브러리를 사용하여 파싱합니다.
//	액세스 토큰을 반환합니다.

//	3. getUserInfo 메서드 설명:
//	인자로 전달된 액세스 토큰을 사용하여 카카오 로그인 API로부터 사용자 정보를 받아옵니다.
//	HttpURLConnection을 사용하여 GET 요청을 보내고, 헤더에 액세스 토큰을 포함합니다.
//	JSON 형식의 응답을 받아와 Gson 라이브러리를 사용하여 파싱합니다.
//	사용자의 닉네임을 반환하는 HashMap에 저장하고 반환합니다.

@Service
public class KakaoService {

	public String getAccessToken (String authorize_code) throws IOException {
		// 사용자 인증 코드를 이용해 액세스 토큰을 얻는 메소드
		String access_Token = "";
		String refresh_Token = "";
		String reqURL = "https://kauth.kakao.com/oauth/token";
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		try {
			URL url = new URL(reqURL);  // 카카오 REST API 요청 URL
            
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// POST 요청을 위해 기본값이 false인 setDoOutput을 true로
            
			conn.setRequestMethod("POST"); // POST 메소드를 사용
			conn.setDoOutput(true); // POST 요청을 위해 OutputStream 사용을 허용
			// POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            
			// 필요한 파라미터를 전송하기 위해 BufferedWriter를 사용

			bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			StringBuilder sb = new StringBuilder();
			sb.append("grant_type=authorization_code"); // 인증 코드 값 전달
            
			sb.append("&client_id=e2758d85befb75965fd0776467743592"); //본인이 발급받은 key
			sb.append("&redirect_uri=http://localhost:8087/kakao/login"); // 본인이  리다이렉트 주소 설정한
			//sb.append("&redirect_uri=http://101.101.216.171:8087/kakao/login");
			
			sb.append("&code=" + authorize_code); // 사용자 인증 코드
			bw.write(sb.toString());
			bw.flush();
            
			// 결과 코드가 200이라면 성공
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);
            
			// 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";
            
			while ((line = br.readLine()) != null) {
				result += line;
			}
			System.out.println("response body : " + result);
            
			// Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
			// Gson 라이브러리를 사용해 JSON 처리
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(result);
            
			access_Token = element.getAsJsonObject().get("access_token").getAsString();
			refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();
            
			System.out.println("access_token : " + access_Token);
			System.out.println("refresh_token : " + refresh_Token);
            
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			br.close();
			bw.close();
		}
		return access_Token;
	}
	
	// 액세스 토큰을 사용하여 사용자 정보를 얻는 메소드
	public HashMap<String, Object> getUserInfo(String access_Token) throws IOException {
		BufferedReader br = null;
		// 요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
		HashMap<String, Object> userInfo = new HashMap<String, Object>(); // 사용자 정보 저장을 위한 HashMap
		String reqURL = "https://kapi.kakao.com/v2/user/me";
		
		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
            // 카카오 토큰을 이용한 GET 요청

			conn.setRequestMethod("GET");

			// 요청에 필요한 Header에 포함될 내용
			// 요청 헤더에 액세스 토큰을 포함

			conn.setRequestProperty("Authorization", "Bearer " + access_Token);
			
            // 응답 코드 확인
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);
			
            // 응답 결과를 JSON으로 변환하여 사용자 정보 추출
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line = "";
			String result = "";

			while ((line = br.readLine()) != null) {
				result += line;
			}
			System.out.println("response body : " + result);
			
			// Gson 라이브러리를 사용해 JSON 처리
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(result);

			JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
			JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

			String nickname = properties.getAsJsonObject().get("nickname").getAsString();
			//String email = kakao_account.getAsJsonObject().get("email").getAsString();
			String id = element.getAsJsonObject().get("id").getAsString();

			userInfo.put("nickname", nickname);
			//userInfo.put("email", email);
			userInfo.put("id", id);


		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			br.close();
		}
		return userInfo;
	}
	
	// 로그아웃
	public void kakaoLogout(String access_Token) { 
		String reqURL = "https://kapi.kakao.com/v1/user/logout"; 
		try { 
		  URL url = new URL(reqURL);
		  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		  conn.setRequestMethod("POST"); 
		  conn.setRequestProperty("Authorization", "Bearer " + access_Token);
		  
		  int responseCode = conn.getResponseCode();
		  System.out.println("responseCode : " + responseCode);
		  
		  BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		  
		  String result = ""; String line = "";
		  
		  while ((line = br.readLine()) != null) { 
			  result += line; 
		  }
		  System.out.println(result); 
	  } catch (IOException e) { 
		  e.printStackTrace(); 
		  }
	  } 
	
	
	
}

/*
 * package com.multi.quizwiki.member.service;
 * 
 * import java.io.BufferedReader; import java.io.BufferedWriter; import
 * java.io.IOException; import java.io.InputStreamReader; import
 * java.io.OutputStreamWriter; import java.net.HttpURLConnection; import
 * java.net.URL; import java.util.HashMap;
 * 
 * import org.springframework.stereotype.Service;
 * 
 * import com.google.gson.JsonElement; import com.google.gson.JsonObject; import
 * com.google.gson.JsonParser;
 * 
 * 
 * @Service public class KakaoService {
 * 
 * public String getAccessToken (String authorize_code) { // 사용자 인증 코드를 이용해 액세스
 * 토큰을 얻는 메소드 String access_Token = ""; String refresh_Token = ""; String reqURL
 * = "https://kauth.kakao.com/oauth/token";
 * 
 * try { URL url = new URL(reqURL); // 카카오 REST API 요청 URL
 * 
 * HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // POST
 * 요청을 위해 기본값이 false인 setDoOutput을 true로
 * 
 * conn.setRequestMethod("POST"); // POST 메소드를 사용 conn.setDoOutput(true); //
 * POST 요청을 위해 OutputStream 사용을 허용 // POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
 * 
 * // 필요한 파라미터를 전송하기 위해 BufferedWriter를 사용
 * 
 * BufferedWriter bw = new BufferedWriter(new
 * OutputStreamWriter(conn.getOutputStream())); StringBuilder sb = new
 * StringBuilder(); sb.append("grant_type=authorization_code"); // 인증 코드 값 전달
 * 
 * sb.append("&client_id=e506eef6e59a3274b5ac0d1a2f0318b6"); //본인이 발급받은 key
 * sb.append("&redirect_uri=http://localhost:8087/kakao/login"); // 본인이 리다이렉트 주소
 * 설정한
 * 
 * sb.append("&code=" + authorize_code); // 사용자 인증 코드 bw.write(sb.toString());
 * bw.flush(); // 요청을 내보내기 위한 정보값 세팅
 * 
 * // 카카오에서 보내오는 response 받기 // 결과 코드가 200이라면 성공 int responseCode =
 * conn.getResponseCode(); System.out.println("responseCode : " + responseCode);
 * 
 * // 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기 BufferedReader br = new
 * BufferedReader(new InputStreamReader(conn.getInputStream())); String line =
 * ""; String result = "";
 * 
 * while ((line = br.readLine()) != null) { result += line; }
 * System.out.println("response body : " + result);
 * 
 * // Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성 // Gson 라이브러리를 사용해 JSON 처리 JsonParser
 * parser = new JsonParser(); JsonElement element = parser.parse(result);
 * 
 * access_Token = element.getAsJsonObject().get("access_token").getAsString();
 * refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();
 * 
 * System.out.println("access_token : " + access_Token);
 * System.out.println("refresh_token : " + refresh_Token);
 * 
 * br.close(); bw.close(); } catch (IOException e) { e.printStackTrace(); }
 * return access_Token; }
 * 
 * // 액세스 토큰을 사용하여 사용자 정보를 얻는 메소드 public HashMap<String, Object>
 * getUserInfo(String access_Token) {
 * 
 * // 요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언 HashMap<String, Object>
 * userInfo = new HashMap<String, Object>(); // 사용자 정보 저장을 위한 HashMap String
 * reqURL = "https://kapi.kakao.com/v2/user/me"; try { URL url = new
 * URL(reqURL); HttpURLConnection conn = (HttpURLConnection)
 * url.openConnection();
 * 
 * // 카카오 토큰을 이용한 GET 요청
 * 
 * conn.setRequestMethod("GET");
 * 
 * // 요청에 필요한 Header에 포함될 내용 // 요청 헤더에 액세스 토큰을 포함
 * 
 * 
 * conn.setRequestProperty("Authorization", "Bearer " + access_Token);
 * 
 * // 응답 코드 확인 int responseCode = conn.getResponseCode();
 * System.out.println("responseCode : " + responseCode);
 * 
 * // 응답 결과를 JSON으로 변환하여 사용자 정보 추출 BufferedReader br = new BufferedReader(new
 * InputStreamReader(conn.getInputStream()));
 * 
 * String line = ""; String result = "";
 * 
 * while ((line = br.readLine()) != null) { result += line; }
 * System.out.println("response body : " + result);
 * System.out.println("=##################userInfo : "); // Gson 라이브러리를 사용해 JSON
 * 처리 JsonParser parser = new JsonParser(); JsonElement element =
 * parser.parse(result);
 * 
 * JsonObject properties =
 * element.getAsJsonObject().get("properties").getAsJsonObject(); JsonObject
 * kakao_account =
 * element.getAsJsonObject().get("kakao_account").getAsJsonObject();
 * System.out.println(properties.toString());
 * System.out.println("--------------------------------------------------------"
 * ); System.out.println(kakao_account.toString()); System.out.println(
 * element.getAsJsonObject().toString()); System.out.println(
 * "----------------------------------------------------------"); String
 * nickname = properties.getAsJsonObject().get("nickname").getAsString();
 * System.out.println(nickname); String email =
 * kakao_account.getAsJsonObject().get("email").getAsString();
 * System.out.println(email); String id =
 * element.getAsJsonObject().get("id").getAsString();
 * System.out.println(nickname+","+email+","+id); userInfo.put("nickname",
 * nickname); userInfo.put("email", email); userInfo.put("id", id);
 * 
 * System.out.println("======================userInfo : "+userInfo); } catch
 * (IOException e) { e.printStackTrace(); } return userInfo; }
 * 
 * // 로그아웃 public void kakaoLogout(String access_Token) { String reqURL =
 * "https://kapi.kakao.com/v1/user/logout"; try { URL url = new URL(reqURL);
 * HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 * conn.setRequestMethod("POST"); conn.setRequestProperty("Authorization",
 * "Bearer " + access_Token);
 * 
 * int responseCode = conn.getResponseCode();
 * System.out.println("responseCode : " + responseCode);
 * 
 * BufferedReader br = new BufferedReader(new
 * InputStreamReader(conn.getInputStream()));
 * 
 * String result = ""; String line = "";
 * 
 * while ((line = br.readLine()) != null) { result += line; }
 * System.out.println(result); } catch (IOException e) { e.printStackTrace(); }
 * } }
 */