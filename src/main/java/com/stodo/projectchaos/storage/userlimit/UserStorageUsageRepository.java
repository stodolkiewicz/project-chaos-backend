package com.stodo.projectchaos.storage.userlimit;

import com.stodo.projectchaos.model.entity.UserStorageUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserStorageUsageRepository extends JpaRepository<UserStorageUsageEntity, UUID> {

    Optional<UserStorageUsageEntity> findByUserId(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserStorageUsageEntity u SET u.usedBytes = u.usedBytes + :bytes WHERE u.user.id = :userId")
    void increaseUsedBytes(@Param("userId") UUID userId, @Param("bytes") long bytes);
}