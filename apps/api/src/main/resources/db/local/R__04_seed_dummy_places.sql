INSERT INTO place (
    name,
    latitude,
    longitude,
    address,
    category,
    view_count,
    is_active,
    is_deleted
)
SELECT
    seed.name,
    seed.latitude,
    seed.longitude,
    seed.address,
    seed.category,
    seed.view_count,
    seed.is_active,
    seed.is_deleted
FROM (
    VALUES
        ('오늘의 식당', 37.57000000, 126.98500000, '서울특별시 종로구 종로 1', 'RESTAURANT', 120, TRUE, FALSE),
        ('종로 카페거리', 37.57200000, 126.98300000, '서울특별시 종로구 인사동길 10', 'CAFE_DESSERT', 95, TRUE, FALSE),
        ('달빛 와인바', 37.56900000, 126.99100000, '서울특별시 종로구 삼일대로 20', 'BAR', 80, TRUE, FALSE),
        ('서울 전시 공간', 37.57700000, 126.98100000, '서울특별시 종로구 율곡로 30', 'EXHIBITION_CULTURE', 75, TRUE, FALSE),
        ('시네마 종로', 37.57000000, 126.97800000, '서울특별시 종로구 새문안로 40', 'MOVIE_PERFORMANCE', 65, TRUE, FALSE),
        ('도예 원데이 클래스', 37.57900000, 126.98900000, '서울특별시 종로구 창덕궁길 50', 'EXPERIENCE_WORKSHOP', 55, TRUE, FALSE),
        ('도심 보드게임 라운지', 37.56800000, 126.98200000, '서울특별시 종로구 종로 60', 'PLAY_ACTIVITY', 45, TRUE, FALSE),
        ('북악산 산책로', 37.59200000, 126.98100000, '서울특별시 종로구 북악산로 70', 'NATURE_WALK', 40, TRUE, FALSE),
        ('인사동 소품 상점', 37.57400000, 126.98500000, '서울특별시 종로구 인사동길 80', 'SHOPPING', 35, TRUE, FALSE),
        ('남산 전망대', 37.55100000, 126.98800000, '서울특별시 중구 남산공원길 90', 'LANDMARK_VIEW', 150, TRUE, FALSE),
        ('도심 스파', 37.56300000, 126.98900000, '서울특별시 중구 을지로 100', 'RELAXATION_SPA', 30, TRUE, FALSE),
        ('운영 중단 장소', 37.56500000, 126.98000000, '서울특별시 종로구 세종대로 110', 'RESTAURANT', 10, FALSE, FALSE),
        ('삭제된 장소', 37.56600000, 126.98100000, '서울특별시 종로구 광화문로 120', 'CAFE_DESSERT', 5, TRUE, TRUE)
) AS seed(name, latitude, longitude, address, category, view_count, is_active, is_deleted)
WHERE NOT EXISTS (
    SELECT 1
    FROM place
    WHERE place.name = seed.name
);

INSERT INTO place_image (
    place_id,
    image_url
)
SELECT
    place.place_id,
    seed.image_url
FROM (
    VALUES
        ('오늘의 식당', 'https://placehold.co/1200x800?text=Restaurant'),
        ('종로 카페거리', 'https://placehold.co/1200x800?text=Cafe'),
        ('달빛 와인바', 'https://placehold.co/1200x800?text=Wine+Bar'),
        ('서울 전시 공간', 'https://placehold.co/1200x800?text=Exhibition'),
        ('시네마 종로', 'https://placehold.co/1200x800?text=Cinema'),
        ('도예 원데이 클래스', 'https://placehold.co/1200x800?text=Workshop'),
        ('도심 보드게임 라운지', 'https://placehold.co/1200x800?text=Board+Game'),
        ('북악산 산책로', 'https://placehold.co/1200x800?text=Nature'),
        ('인사동 소품 상점', 'https://placehold.co/1200x800?text=Shopping'),
        ('남산 전망대', 'https://placehold.co/1200x800?text=Landmark'),
        ('도심 스파', 'https://placehold.co/1200x800?text=Spa'),
        ('운영 중단 장소', 'https://placehold.co/1200x800?text=Inactive'),
        ('삭제된 장소', 'https://placehold.co/1200x800?text=Deleted')
) AS seed(place_name, image_url)
JOIN place ON place.name = seed.place_name
WHERE NOT EXISTS (
    SELECT 1
    FROM place_image
    WHERE place_image.place_id = place.place_id
      AND place_image.image_url = seed.image_url
);

INSERT INTO region_place (
    region_id,
    place_id
)
SELECT
    region.region_id,
    place.place_id
FROM (
    VALUES
        ('오늘의 식당', '종로구'),
        ('종로 카페거리', '종로구'),
        ('달빛 와인바', '종로구'),
        ('서울 전시 공간', '종로구'),
        ('시네마 종로', '종로구'),
        ('도예 원데이 클래스', '종로구'),
        ('도심 보드게임 라운지', '종로구'),
        ('북악산 산책로', '종로구'),
        ('인사동 소품 상점', '종로구'),
        ('남산 전망대', '중구'),
        ('도심 스파', '중구'),
        ('운영 중단 장소', '종로구'),
        ('삭제된 장소', '종로구')
) AS seed(place_name, gu)
JOIN place ON place.name = seed.place_name
JOIN region
  ON region.si = '서울특별시'
 AND region.gun = '해당 없음'
 AND region.gu = seed.gu
WHERE NOT EXISTS (
    SELECT 1
    FROM region_place
    WHERE region_place.region_id = region.region_id
      AND region_place.place_id = place.place_id
);

INSERT INTO place (
    name,
    latitude,
    longitude,
    address,
    category,
    view_count,
    is_active,
    is_deleted
)
SELECT
    seed.region_name || ' 대표 장소',
    36.00000000 + MOD(seed.region_id, 200) * 0.01000000,
    126.00000000 + MOD(seed.region_id, 300) * 0.01000000,
    seed.region_name || ' 중앙로 1',
    CASE MOD(seed.region_id, 11)
        WHEN 0 THEN 'RESTAURANT'
        WHEN 1 THEN 'CAFE_DESSERT'
        WHEN 2 THEN 'BAR'
        WHEN 3 THEN 'EXHIBITION_CULTURE'
        WHEN 4 THEN 'MOVIE_PERFORMANCE'
        WHEN 5 THEN 'EXPERIENCE_WORKSHOP'
        WHEN 6 THEN 'PLAY_ACTIVITY'
        WHEN 7 THEN 'NATURE_WALK'
        WHEN 8 THEN 'SHOPPING'
        WHEN 9 THEN 'LANDMARK_VIEW'
        ELSE 'RELAXATION_SPA'
    END,
    MOD(seed.region_id * 7, 200),
    TRUE,
    FALSE
FROM (
    SELECT
        region.region_id,
        CASE
            WHEN region.gun = '해당 없음' AND region.gu = '해당 없음' THEN region.si
            WHEN region.gun = '해당 없음' THEN region.si || ' ' || region.gu
            WHEN region.gu = '해당 없음' THEN region.si || ' ' || region.gun
            ELSE region.si || ' ' || region.gun || ' ' || region.gu
        END AS region_name
    FROM region
) AS seed
WHERE NOT EXISTS (
    SELECT 1
    FROM place
    WHERE place.name = seed.region_name || ' 대표 장소'
);

INSERT INTO place_image (
    place_id,
    image_url
)
SELECT
    place.place_id,
    'https://placehold.co/1200x800?text=TodayIt+Place'
FROM place
WHERE place.name LIKE '% 대표 장소'
  AND NOT EXISTS (
      SELECT 1
      FROM place_image
      WHERE place_image.place_id = place.place_id
        AND place_image.image_url = 'https://placehold.co/1200x800?text=TodayIt+Place'
  );

INSERT INTO region_place (
    region_id,
    place_id
)
SELECT
    region.region_id,
    place.place_id
FROM region
JOIN place
  ON place.name =
      CASE
          WHEN region.gun = '해당 없음' AND region.gu = '해당 없음'
              THEN region.si || ' 대표 장소'
          WHEN region.gun = '해당 없음'
              THEN region.si || ' ' || region.gu || ' 대표 장소'
          WHEN region.gu = '해당 없음'
              THEN region.si || ' ' || region.gun || ' 대표 장소'
          ELSE region.si || ' ' || region.gun || ' ' || region.gu || ' 대표 장소'
      END
WHERE NOT EXISTS (
    SELECT 1
    FROM region_place
    WHERE region_place.region_id = region.region_id
      AND region_place.place_id = place.place_id
);
