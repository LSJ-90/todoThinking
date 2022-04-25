##  INTERVAL

 문 법: INTERVAL '간격을 알고싶은 시간' 년,월,일 etc

```SQL
SYSDATE + (INTERVAL '1' YEAR)        --1년 더하기
SYSDATE + (INTERVAL '1' MONTH)     --1개월 더하기
SYSDATE + (INTERVAL '1' DAY)        --1일 더하기
SYSDATE + (INTERVAL '1' HOUR)       --1시간 더하기
SYSDATE + (INTERVAL '1' MINUTE)     --1분 더하기
SYSDATE + (INTERVAL '1' SECOND)    --1초 더하기
SYSDATE + (INTERVAL '02:10' HOUR TO MINUTE)    --2시간10분 더하기
SYSDATE + (INTERVAL '01:30' MINUTE TO SECOND) --1분30초 더하기
```