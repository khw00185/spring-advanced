# SPRING ADVANCED

1. [문제 인식 및 정의]
   기존 인증 시스템에서는 Access Token만을 사용하고 있었으며, 해당 토큰의 유효기간이 1시간으로 설정되어 있었음.
   그러나 다음과 같은 보안 문제가 있었다:
      - Access Token 탈취 시, 1시간 동안 무방비 상태
      - 탈취된 Access Token이 만료되기 전까지는 임의의 사용자가 시스템을 악용할 가능성이 존재함.
      - 토큰이 탈취된 경우, 해당 사용자가 로그아웃할 방법이 없음.

   따라서 안전한 인증 방식을 위해 Refresh Token을 도입할 필요성을 느꼈음.


2. [해결 방안]
   2-1. [의사결정 과정]
   기존의 단일 Access Token 방식은 보안 취약점이 존재함.
   Refresh Token을 도입하면 다음과 같은 이점이 있음:
   - Access Token의 유효기간을 10분으로 단축하여 탈취 시 피해를 최소화할 수 있음.
   - Refresh Token을 통해 토큰 재발급을 허용하면서, 보안성을 유지할 수 있음.
   - 토큰 로테이션(Token Rotation) 기법을 적용하여, Refresh Token이 재사용되지 않도록 보완 가능.
   
   2-2. [해결 과정]
   토큰 발급 방식 변경:
      Access Token 1시간을 Access Token 10분 + Refresh Token 7일으로 변경.
      Refresh Token을 DB에서 관리.
      탈취 시 대처가 불가능했던 기존의 코드에서 탈취 위험성을 줄이고, Refresh Token으로 안전한 재발급.
   토큰 로테이션(Token Rotation) 적용
      기존 Refresh Token이 사용될 경우, 새로운 Refresh Token을 재발급하여 이전 토큰을 무효화함.
      이를 통해, 탈취된 Refresh Token이 악용되는 것을 방지함.
      발급된 Refresh Token과 요청이 들어온 Refresh Token이 다를 경우 비정상적인 접근 처리를 위해 IllegalArgumentException 처리 후 로그 작성

   Refresh Token의 보안 강화를 위해 다음과 같은 조치를 추가
      Refresh Token은 HTTP Only, Secure Cookie로 저장하여 XSS(크로스 사이트 스크립팅) 공격을 방어함. (테스트를 위해 Secure는 잠시 제외함.)
      
   
3. [해결 완료]
   3-1. [회고]
   -	기존보다 안전한 인증 시스템 구축
   -	Access Token의 유효시간을 단축하여 탈취 시 피해 최소화
   -	Refresh Token을 통해 안정적인 인증 유지 가능
   -	Refresh Token의 로테이션을 통해 토큰 재사용 공격 방지
   -	보안성이 증가하면서도, 사용자 경험을 고려한 인증 방식 도입
   -	로그인 빈도를 줄이면서도, 높은 보안 수준을 유지할 수 있음
   3-2. [전후 데이터 비교]
      ResponseBody에 jwt토큰 하나만 발급 -> Access, Refresh Token 두개 발급 (Refresh Token은 헤더에 Set-Cookie하여 저장)