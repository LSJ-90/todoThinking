## formData()

이미지 파일을 업로드 할 때 사용가능함
form 태그내의 값들을 ajax로 보낼 때도 사용가능

# forData() 사용 시 빈 생성
스프링에서 formData 사용 시 multipartResolver bean을 생성해줘야함(dispatcher-servlet.xml)
<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver"/>
>> 스프링에서 Multipart 기능을 사용하기 위해 MultipartResolver를 등록.
   MultipartResolver는 CommmnosMultipartResolver 클래스를 빈으로 등록.


# Error creating bean with name ‘multipartResolver’: Lookup method resolution failed 발생 시 pom.xml 디펜던시 추가
>> CommmnsMultipartResolver클래스는 CommonsFileUpload API를 이용하는데, CommonsFileUpload API를 찾을 수 없어서 발생하는 오류
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.4</version>
</dependency>

      