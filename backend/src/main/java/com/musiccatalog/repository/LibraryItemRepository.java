package com.musiccatalog.repository;

import com.musiccatalog.entity.LibraryItem;
import com.musiccatalog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibraryItemRepository extends JpaRepository<LibraryItem, Long> {

    Page<LibraryItem> findByUser(User user, Pageable pageable);

    Optional<LibraryItem> findByIdAndUser(Long id, User user);

    boolean existsByAppleCatalogIdAndUser(Long appleCatalogId, User user);

    List<LibraryItem> findByUser(User user);

    @Query("SELECT li.genre, COUNT(li) FROM LibraryItem li WHERE li.user = :user AND li.genre IS NOT NULL GROUP BY li.genre")
    List<Object[]> countByGenre(User user);

    @Query("SELECT EXTRACT(YEAR FROM li.releaseDate), COUNT(li) FROM LibraryItem li WHERE li.user = :user AND li.releaseDate IS NOT NULL GROUP BY EXTRACT(YEAR FROM li.releaseDate) ORDER BY EXTRACT(YEAR FROM li.releaseDate)")
    List<Object[]> countByReleaseYear(User user);

    @Query("SELECT li.artistName, COUNT(li) FROM LibraryItem li WHERE li.user = :user GROUP BY li.artistName ORDER BY COUNT(li) DESC")
    List<Object[]> countByArtist(User user);

    @Query("SELECT li.genre, AVG(li.userRating) FROM LibraryItem li WHERE li.user = :user AND li.genre IS NOT NULL AND li.userRating IS NOT NULL GROUP BY li.genre")
    List<Object[]> averageRatingByGenre(User user);

    @Query("SELECT AVG(li.userRating) FROM LibraryItem li WHERE li.user = :user AND li.userRating IS NOT NULL")
    Double averageUserRating(User user);

    @Query("SELECT li.trackCount FROM LibraryItem li WHERE li.user = :user AND li.trackCount IS NOT NULL")
    List<Integer> findTrackCounts(User user);
}
