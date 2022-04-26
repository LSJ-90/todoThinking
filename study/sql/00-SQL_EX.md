## SQL_EX


```SQL
/*
    Q6. 판매왕 찾기
       - 2017년에 가장 많은 금액을 판매한 사원의 이름과 직책, 그리고 담당매니저의 이름과 직책을 구하세요.
            1269323.77	Freya Gomez	Sales Representative	Ella Wallace	Sales Manager  
*/ 
SELECT RANK_TOTAL_SALES_INFO_TAB.TOTAL_SALES, 
       RANK_TOTAL_SALES_INFO_TAB.SALESMAN_NAME, 
       RANK_TOTAL_SALES_INFO_TAB.SALESMAN_JOB, 
       RANK_TOTAL_SALES_INFO_TAB.MANAGER_NAME, 
       RANK_TOTAL_SALES_INFO_TAB.MANAGER_JOB
FROM (
      SELECT B.ORDER_ID, 
             SUM(B.QUANTITY*B.UNIT_PRICE) AS TOTAL_SALES,
             RANK() OVER(ORDER BY SUM(B.QUANTITY*B.UNIT_PRICE) DESC) AS RANK_TOTAL_SALES,
             C.FIRST_NAME || ' ' || C.LAST_NAME AS SALESMAN_NAME,
             C.JOB_TITLE AS SALESMAN_JOB,
             D.FIRST_NAME || ' ' || D.LAST_NAME AS MANAGER_NAME,
             D.JOB_TITLE AS MANAGER_JOB
      FROM SJ_ORDERS A
      INNER JOIN SJ_ORDER_ITEMS B ON A.ORDER_ID = B.ORDER_ID 
      AND TO_CHAR(A.ORDER_DATE, 'YYYY') = '2017'
      AND NOT A.STATUS = 'Canceled'
      LEFT OUTER JOIN SJ_EMPLOYEES C ON A.SALESMAN_ID = C.EMPLOYEE_ID
      LEFT OUTER JOIN SJ_EMPLOYEES D ON C.MANAGER_ID = D.EMPLOYEE_ID
      GROUP BY B.ORDER_ID,
               C.FIRST_NAME || ' ' || C.LAST_NAME,
               C.JOB_TITLE,
               D.FIRST_NAME || ' ' || D.LAST_NAME,
               D.JOB_TITLE
      ) RANK_TOTAL_SALES_INFO_TAB
WHERE RANK_TOTAL_SALES_INFO_TAB.RANK_TOTAL_SALES = 1;

-- 랭크 확인
SELECT B.ORDER_ID,
       A.STATUS,
       A.ORDER_DATE,
       SUM(B.QUANTITY*B.UNIT_PRICE) AS TOTAL_SALES,
       A.SALESMAN_ID
FROM SJ_ORDERS A
INNER JOIN SJ_ORDER_ITEMS B ON A.ORDER_ID = B.ORDER_ID 
WHERE TO_CHAR(A.ORDER_DATE, 'YYYY') = '2017'
AND NOT A.STATUS = 'Canceled'
GROUP BY B.ORDER_ID, A.STATUS, A.ORDER_DATE, A.SALESMAN_ID;

-- 사원확인
SELECT C.*
FROM SJ_EMPLOYEES C;

/*
    Q7. 지점간 재고이동
        - 전체 지역의 창고의 물품 중 현재 가장 재고가 많이 남은 제품명과 소속 WAREHOUSE 이름을 찾고, 
          전체 지역의 창고 중 찾은 제품의 재고가 가장 적은 WAREHOUSE의 이름과 제품의 현재 재고, 배송을 보낼 주소를 찾아주세요.
          1. 전체 지역의 창고에서 재고량에 대해 순위를 조회 한 후 재고가 가장 많은 품목의 정보를 조회한다.
          2. 조회된 품목을 가지고 있는 창고정보를 조회한다.
          3. 그 창고 중에서도 재고량이 가장 많은 창고정보를 조회한다.
          4. 현재 가장 재고가 많이 남은 제품명과 소속 창고의 이름을 조인을 통해 찾는다. -- 1번 결과값
          
          5. 전체 지역의 창고에서 재고량에 대해 순위를 조회 한 후 재고가 가장 많은 품목의 정보를 조회한다.
          6. 조회된 품목을 가지고 있는 창고정보를 조회한다.
          7. 그 창고 중에서도 재고량이 가장 적은 창고정보를 조회한다.
          8. 재고량이 가장 적은 창고의 이름과 주소, 현재 재고량을 조인을 통해 찾는다. -- 2번 결과값
          9. 1번 결과값을 배송될 품목 아이디를 조인키로 하여 2번 결과값과 조인한다. 
*/
WITH MAX_QUANTITY_PRODUCT_INFO_TAB AS 
(
SELECT A.PRODUCT_ID, -- 재고가 가장 많은 품목을 가진 창고정보를 조회
       A.WAREHOUSE_ID, 
       A.QUANTITY
FROM SJ_INVENTORIES A
WHERE A.PRODUCT_ID IN (
                       SELECT RANK_MAX_TAB.PRODUCT_ID  -- 재고가 가장 많은 품목 아이디를 조회
                       FROM (
                             SELECT PRODUCT_ID, -- 모든 창고에서 재고량에 대해 내림차순 순위를 조회
                                    QUANTITY, 
                                    RANK() OVER(ORDER BY QUANTITY DESC) RANK_MAX_QUANTITY
                             FROM SJ_INVENTORIES
                             ) RANK_MAX_TAB
                       WHERE RANK_MAX_TAB.RANK_MAX_QUANTITY = 1
                      )
)

SELECT D.WAREHOUSE_NAME AS SEND_WAREHOUSE_NAME,
       H.PRODUCT_NAME AS SEND_PRODUCT,
       MIN_INFO_DETAIL_TAB.RECEIVER_WAREHOUSE_NAME,
       MIN_INFO_DETAIL_TAB.RECEIVER_WAREHOUSE_ADDRESS,
       MIN_INFO_DETAIL_TAB.RECEIVER_WAREHOUSE_STOCK
FROM (
      SELECT PRODUCT_ID, -- 재고가 가장 많은 품목을 가진 창고 중에 가장 많은 재고량을 가진 창고를 조회
             WAREHOUSE_ID, 
             QUANTITY
      FROM (
            SELECT PRODUCT_ID,  -- 재고가 가장 많은 품목을 가진 창고들의 내림차순 순위를 조회
                   WAREHOUSE_ID, 
                   QUANTITY, 
                   RANK() OVER(ORDER BY QUANTITY DESC) AS RANK_MAX_QUANTITY_BY_WAREHOUSE
            FROM MAX_QUANTITY_PRODUCT_INFO_TAB
            )
      WHERE RANK_MAX_QUANTITY_BY_WAREHOUSE = 1
      ) MAX_INFO_TAB
INNER JOIN SJ_PRODUCTS H ON MAX_INFO_TAB.PRODUCT_ID = H.PRODUCT_ID
INNER JOIN SJ_WAREHOUSES D ON MAX_INFO_TAB.WAREHOUSE_ID = D.WAREHOUSE_ID
INNER JOIN (
            SELECT D.WAREHOUSE_NAME AS RECEIVER_WAREHOUSE_NAME,
                   E.ADDRESS || ',' || E.CITY || ',' || E.STATE || ',' || E.POSTAL_CODE || ',' || F.COUNTRY_NAME AS RECEIVER_WAREHOUSE_ADDRESS,
                   MIN_INFO_TAB.QUANTITY AS RECEIVER_WAREHOUSE_STOCK,
                   MIN_INFO_TAB.PRODUCT_ID
            FROM (
                  SELECT PRODUCT_ID, -- 재고가 가장 많은 품목을 가진 창고 중에 가장 적은 재고량을 가진 창고를 조회
                         WAREHOUSE_ID, 
                         QUANTITY
                  FROM (
                        SELECT PRODUCT_ID, -- 재고가 가장 많은 품목을 가진 창고들의 오름차순 순위를 조회
                               WAREHOUSE_ID, 
                               QUANTITY, 
                               RANK() OVER(ORDER BY QUANTITY ASC) AS RANK_MIN_QUANTITY_BY_WAREHOUSE
                        FROM MAX_QUANTITY_PRODUCT_INFO_TAB
                        )
                  WHERE RANK_MIN_QUANTITY_BY_WAREHOUSE = 1
                  ) MIN_INFO_TAB
                  INNER JOIN SJ_PRODUCTS H ON MIN_INFO_TAB.PRODUCT_ID = H.PRODUCT_ID
                  INNER JOIN SJ_WAREHOUSES D ON MIN_INFO_TAB.WAREHOUSE_ID = D.WAREHOUSE_ID
                  INNER JOIN SJ_LOCATIONS E ON D.LOCATION_ID = E.LOCATION_ID
                  INNER JOIN SJ_COUNTRIES F ON E.COUNTRY_ID = F.COUNTRY_ID
                  INNER JOIN SJ_REGIONS G ON F.REGION_ID = G.REGION_ID
            ) MIN_INFO_DETAIL_TAB
ON MAX_INFO_TAB.PRODUCT_ID = MIN_INFO_DETAIL_TAB.PRODUCT_ID;

/*
    Q8. 등급별 고객사 프로모션을 위한 메타정보 조회
        - 금액한도(CREDIT_LIMIT) 별로 '파트'를 나누어 고객정보를 표현해주세요.
          ex) 금액한도가 5000인 고객정보들...
              금액한도가 3500인 고객정보들...
        1.
*/
SELECT A.CREDIT_LIMIT,
       ROW_NUMBER() OVER(PARTITION BY CREDIT_LIMIT ORDER BY CUSTOMER_ID),
       A.CUSTOMER_ID, 
       A.NAME AS COMPANY_NAME, 
       A.ADDRESS, 
       A.WEBSITE,
       b.first_name || ' ' || b.last_name AS MANAGER_NAME,
       b.email,
       b.phone
FROM SJ_CUSTOMERS A
LEFT OUTER JOIN SJ_CONTACTS B ON A.CUSTOMER_ID = B.CUSTOMER_ID;
ORDER BY CREDIT_LIMIT DESC, CUSTOMER_ID ASC;

SELECT A.CREDIT_LIMIT,
       COUNT(*) OVER(PARTITION BY A.CREDIT_LIMIT)  AS  CREDIT_LIMIT_COUNT,
       A.CUSTOMER_ID, 
       A.NAME AS COMPANY_NAME, 
       A.ADDRESS, 
       A.WEBSITE,
       b.first_name || ' ' || b.last_name AS MANAGER_NAME,
       b.email,
       b.phone
FROM SJ_CUSTOMERS A
LEFT OUTER JOIN SJ_CONTACTS B ON A.CUSTOMER_ID = B.CUSTOMER_ID
ORDER BY CREDIT_LIMIT DESC, CUSTOMER_ID ASC;
/* LISTAGG를 사용하여 고객사명 나열
SELECT CREDIT_LIMIT, 
       LISTAGG(NAME, ',') WITHIN GROUP(ORDER BY NAME) AS COMPANY_NAMES
FROM SJ_CUSTOMERS 
GROUP BY CREDIT_LIMIT
ORDER BY CREDIT_LIMIT DESC;
*/

/*
    Q9. 전년도 인기상품 세일 
        - 2017년 매출액 1위 상품의 상품 카테고리를 포함한 모든 상품정보, 그리고 10% 할인된 LIST_PRICE의 할인가를 구해주세요.
        1. 2017년 판매된 모든 품목아이디와 상품 별 총 매출을 조회한다.
        2. 품목 별 총 매출의 내림차순 순위를 조회한다.
        3. 1위인 품목에 대한 요청 정보를 조회한다.
*/
SELECT RANK_PRODUCT_INFO_TAB.SALES_RANK,
       RANK_PRODUCT_INFO_TAB.TOTAL_SALES_BY_PRODUCT AS TOP_TOTAL_SALES,
       RANK_PRODUCT_INFO_TAB.PRODUCT_ID,
       C.PRODUCT_NAME,
       C.DESCRIPTION,
       C.STANDARD_COST,
       C.LIST_PRICE,
       (C.LIST_PRICE*0.9) AS "DISCOUNT_LIST_PRICE(10%)", -- 10% 할인된 LIST_PRICE의 할인가
       D.CATEGORY_ID,
       D.CATEGORY_NAME
FROM (
      SELECT RANK() OVER(ORDER BY TOTAL_SALES_BY_PRODUCT DESC) AS SALES_RANK, -- 품목 별 총 매출의 내림차순 순위를 조회
             TOTAL_SALES_BY_PRODUCT_TAB.TOTAL_SALES_BY_PRODUCT, 
             TOTAL_SALES_BY_PRODUCT_TAB.PRODUCT_ID
      FROM (
            SELECT A.PRODUCT_ID, -- 2017년 판매된 모든 품목 아이디와 품목 별 총 매출을 조회
                   SUM(A.QUANTITY * A.UNIT_PRICE) AS TOTAL_SALES_BY_PRODUCT 
            FROM SJ_ORDER_ITEMS A
            INNER JOIN SJ_ORDERS B ON A.ORDER_ID = B.ORDER_ID
            AND TO_CHAR(B.ORDER_DATE, 'YYYY') = '2017'
            AND NOT B.STATUS = 'Canceled'
            GROUP BY A.PRODUCT_ID
           ) TOTAL_SALES_BY_PRODUCT_TAB
      ) RANK_PRODUCT_INFO_TAB
INNER JOIN SJ_PRODUCTS C ON RANK_PRODUCT_INFO_TAB.PRODUCT_ID = C.PRODUCT_ID
INNER JOIN SJ_PRODUCT_CATEGORIES D ON C.CATEGORY_ID = D.CATEGORY_ID
WHERE SALES_RANK = 1;

/*
    Q10. Pending(지연) 현황 찾기
        - 현재 시간 기준 6년 이상 Pending(지연)된 건수를 찾아내고 담당 판매사원별로 조회해주세요.
        1. 상태가 지연 중인 건 수 중 6년 이상된 건 수를 조회한다.
        2. 지연 중인 건 수의 담당 판매사원을 조인하여 조회한다.
*/
-- INTERVAL '차이를 계산하고 싶은 값' 년,월,일,시,분,초
SELECT A.SALESMAN_ID,
       A.ORDER_ID, 
       A.STATUS, 
       TO_CHAR(A.ORDER_DATE, 'YYYY-MM-DD') AS ORDER_DATE,
       B.FIRST_NAME || ' ' || B.LAST_NAME AS SALESMAN_NAME,
       B.EMAIL,
       B.PHONE,
       B.HIRE_DATE,
       B.MANAGER_ID,
       B.JOB_TITLE
FROM SJ_ORDERS A
LEFT OUTER JOIN SJ_EMPLOYEES B ON A.SALESMAN_ID = B.EMPLOYEE_ID
WHERE A.STATUS = 'Pending' 
AND ORDER_DATE <= SYSDATE - (INTERVAL '6' YEAR)
ORDER BY A.SALESMAN_ID;

-- ADD_MONTHS(SYSDATE, 계산하고 싶은 달의 수를 표시)
SELECT A.ORDER_ID, 
       A.STATUS, 
       A.ORDER_DATE, 
       A.SALESMAN_ID,
       B.FIRST_NAME || ' ' || B.LAST_NAME AS SALESMAN_NAME,
       B.EMAIL,
       B.PHONE,
       B.HIRE_DATE,
       B.MANAGER_ID,
       B.JOB_TITLE
FROM SJ_ORDERS A
LEFT OUTER JOIN SJ_EMPLOYEES B ON A.SALESMAN_ID = B.EMPLOYEE_ID
WHERE STATUS = 'Pending' 
AND ORDER_DATE <= ADD_MONTHS(SYSDATE, -72)
ORDER BY ORDER_ID;

-- 계산하고 싶은 날짜 수를 모두 표시
SELECT A.ORDER_ID, 
       A.STATUS, 
       A.ORDER_DATE, 
       A.SALESMAN_ID,
       B.FIRST_NAME || ' ' || B.LAST_NAME AS SALESMAN_NAME,
       B.EMAIL,
       B.PHONE,
       B.HIRE_DATE,
       B.MANAGER_ID,
       B.JOB_TITLE
FROM SJ_ORDERS A
LEFT OUTER JOIN SJ_EMPLOYEES B ON A.SALESMAN_ID = B.EMPLOYEE_ID
WHERE STATUS = 'Pending' 
AND ORDER_DATE <= SYSDATE - 2191
ORDER BY ORDER_ID;

/*
    Q11. 연도별 판매현황
        - 연도별 총 판매금액, 누적판매금액을 조회해주세요.
    1. 연도별 총 판매금액을 구한다.
    2. 누적판매금액을 구하기 위해 셀프 조인하여 누적판매금액을 조회한다.
*/
SELECT TAB_1.YEAR,
       '$ ' || TO_CHAR(TAB_1.TOTAL_SALES, 'FM999,999,999,999.00') AS SALES_BY_YEAR,
       '$ ' || TO_CHAR(SUM(TAB_2.TOTAL_SALES), 'FM999,999,999,999.00') AS CUMULATIVE_SALES
FROM (
      SELECT TO_CHAR(ORDER_DATE, 'YYYY') AS YEAR, 
             SUM(A.QUANTITY*A.UNIT_PRICE)  AS TOTAL_SALES 
      FROM SJ_ORDER_ITEMS A
      INNER JOIN SJ_ORDERS B ON A.ORDER_ID = B.ORDER_ID
      AND NOT B.STATUS = 'Canceled'
      GROUP BY TO_CHAR(ORDER_DATE, 'YYYY')
      ) TAB_1
INNER JOIN (
            SELECT TO_CHAR(ORDER_DATE, 'YYYY') AS YEAR, 
                   SUM(A.QUANTITY*A.UNIT_PRICE)  AS TOTAL_SALES 
            FROM SJ_ORDER_ITEMS A
            INNER JOIN SJ_ORDERS B ON A.ORDER_ID = B.ORDER_ID
            AND NOT B.STATUS = 'Canceled'
            GROUP BY TO_CHAR(ORDER_DATE, 'YYYY')
            ) TAB_2
ON TAB_1.YEAR >= TAB_2.YEAR
GROUP BY TAB_1.YEAR,
         '$ ' || TO_CHAR(TAB_1.TOTAL_SALES, 'FM999,999,999,999.00')
ORDER BY TAB_1.YEAR;

-- 누적 매출 CHECK
SELECT SUM(A.QUANTITY*A.UNIT_PRICE)
FROM SJ_ORDER_ITEMS A
INNER JOIN SJ_ORDERS B ON A.ORDER_ID = B.ORDER_ID 
AND NOT B.STATUS = 'Canceled';

/*
    Q12. 
        고객별 총 판매금액, 전체 비중을 구하고 상위 10명의 고객 명단을 작성해주세요.
        (여기서 말하는 비중이란 판매금액 기준 상위 10명의 고객이 차지하는 고객별 퍼센테이지입니다.)
    1. 'Canceled'되지 않은 매출(QUANTITY*UNIT_PRICE)의 총합을 구해 총 매출을 구한다. 총 매출: 43239585.55
    2. 'Canceled'되지 않은 고객사 별 매출(QUANTITY*UNIT_PRICE)을 구한다.
    3. 고객사 별 매출의 순위를 따진다.
    4. 필요한 정보가 있는 테이블을 조인 한 뒤 상위 10명의 조건을 걸고 고객별 총 판매금액과 비중을 구한다.
*/
WITH TOTAL_SALES_BY_CUSTOMER_ID_TAB AS -- 고객사별 매출 테이블
(
SELECT B.CUSTOMER_ID, 
       SUM(A.QUANTITY*A.UNIT_PRICE) AS TOTAL_SALES_BY_CUSTOMER_ID
FROM SJ_ORDER_ITEMS A
INNER JOIN SJ_ORDERS B ON A.ORDER_ID = B.ORDER_ID
AND NOT B.STATUS = 'Canceled'
GROUP BY B.CUSTOMER_ID
)

SELECT RANK_SALES_BY_CUSTOMER || '위' AS RANK,
       ROUND((TOTAL_SALES_BY_CUSTOMER_ID/TOTAL_ALL_SALES)*100, 2) || '%' AS Sales_Portion, -- 비중=(고객사별 매출 / 총 매출) * 100
       RANK_SALES_BY_CUSTOMER_TAB.CUSTOMER_ID,
       C.NAME AS COMPANY_NAME,
       C.ADDRESS,
       C.WEBSITE,
       C.CREDIT_LIMIT,
       RANK_SALES_BY_CUSTOMER_TAB.TOTAL_SALES_BY_CUSTOMER_ID
FROM (
      SELECT CUSTOMER_ID, 
             TOTAL_SALES_BY_CUSTOMER_ID, 
             RANK() OVER(ORDER BY TOTAL_SALES_BY_CUSTOMER_ID DESC) AS RANK_SALES_BY_CUSTOMER,
            (
             SELECT SUM(TOTAL_SALES_BY_CUSTOMER_ID) 
             FROM TOTAL_SALES_BY_CUSTOMER_ID_TAB 
             ) TOTAL_ALL_SALES -- 총 매출정보
      FROM TOTAL_SALES_BY_CUSTOMER_ID_TAB 
      ) RANK_SALES_BY_CUSTOMER_TAB -- 고객아이디, 고객사별 총 매출, 고객사별 총 매출 랭크, 총 매출을 나타내는 테이블
INNER JOIN SJ_CUSTOMERS C ON RANK_SALES_BY_CUSTOMER_TAB.CUSTOMER_ID = C.CUSTOMER_ID
WHERE RANK_SALES_BY_CUSTOMER <= 10
ORDER BY RANK_SALES_BY_CUSTOMER;
```