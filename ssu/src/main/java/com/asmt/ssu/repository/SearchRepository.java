package com.asmt.ssu.repository;

import com.asmt.ssu.domain.SearchDTO;
import com.asmt.ssu.form.SearchForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SearchRepository {
    private final EntityManager em;

    private static final int elementCountInPage = 150;


    public List<SearchDTO> findResultByLowPrice(SearchForm searchForm) {
        return em.createQuery("select new com.asmt.ssu.domain.SearchDTO(p.placeName, p.placeAddress, p.placeRating, p.placeLink, p.placeDistance, p.school, m.id, m.menuName, m.menuPrice, m.menuImg, CASE WHEN b.menu.id IS NULL THEN false ELSE true END)" +
                        " from Menu m join m.place p left join Bookmark b on b.menu = m and b.userId = :userId where p.school = :school and m.menuPrice between :minValue and :maxValue order by "+ searchForm.makeSortResult(), SearchDTO.class)
                .setParameter("school", searchForm.getSchool())
                .setParameter("minValue", searchForm.getMinimumPrice())
                .setParameter("maxValue", searchForm.getMaximumPrice())
                .setParameter("userId", searchForm.getUserId())
                .setFirstResult((searchForm.getPage() - 1) * elementCountInPage)
                .setMaxResults(elementCountInPage)
                .getResultList();
    }

    public List<SearchDTO> findResultByPriceWithName(SearchForm searchForm) {
        StringBuilder jpqlBuilder = new StringBuilder();
        for (int i = 0; i < searchForm.getSearchKeywordList().size(); i++) {
            if (i > 0) {
                jpqlBuilder.append(" OR ");
            }
            jpqlBuilder.append("m.menuName LIKE :searchString").append(i);
        }
        String jpql = jpqlBuilder.toString();
        TypedQuery<SearchDTO> resultQuery = em.createQuery("select new com.asmt.ssu.domain.SearchDTO(p.placeName, p.placeAddress, p.placeRating, p.placeLink, p.placeDistance, p.school, m.id, m.menuName, m.menuPrice, m.menuImg, " +
                        "CASE WHEN b.menu.id IS NULL THEN false ELSE true END)" +
                        " from Menu m join m.place p left join Bookmark b  on b.menu = m and b.userId = :userId where p.school = :school and (" + jpql + ") and  m.menuPrice between :minValue and :maxValue order by "+ searchForm.makeSortResult(), SearchDTO.class)
                .setParameter("school", searchForm.getSchool())
                .setParameter("minValue", searchForm.getMinimumPrice())
                .setParameter("maxValue", searchForm.getMaximumPrice())
                .setParameter("userId", searchForm.getUserId())
                .setFirstResult((searchForm.getPage() - 1) * elementCountInPage)
                .setMaxResults(elementCountInPage);
        for (int i = 0; i < searchForm.getSearchKeywordList().size(); i++) {
            resultQuery.setParameter("searchString" + i, "%" + searchForm.getSearchKeywordList().get(i) + "%");
        }
        return resultQuery.getResultList();
    }
}
