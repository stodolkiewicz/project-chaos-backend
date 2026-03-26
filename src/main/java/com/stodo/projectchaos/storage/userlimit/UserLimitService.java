package com.stodo.projectchaos.storage.userlimit;

import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.entity.UserStorageUsageEntity;
import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.storage.userlimit.dto.service.UserStorageUsage;
import com.stodo.projectchaos.storage.userlimit.dto.mapper.UserStorageUsageEntityMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserLimitService {
    
    private final UserStorageUsageRepository userStorageUsageRepository;
    private final UserRepository userRepository;
    
    @Value("${storage.limits.user.default}")
    private long defaultUserLimit;

    public boolean canUpload(UUID userId, long bytesToUpload) {
        UserStorageUsageEntity usage = userStorageUsageRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User storage usage not found for user: " + userId));
        
        return (usage.getUsedBytes() + bytesToUpload) <= usage.getLimitBytes();
    }

    public void increaseUsedBytes(UUID userId, long bytes) {
        userStorageUsageRepository.increaseUsedBytes(userId, bytes);
    }

    public void createUserStorageUsage(UUID userId) {
        UserEntity user = userRepository.getReferenceById(userId);
        
        UserStorageUsageEntity usage = UserStorageUsageEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .usedBytes(0L)
                .limitBytes(defaultUserLimit)
                .build();
        
        userStorageUsageRepository.save(usage);
    }

    public UserStorageUsage getUserStorageUsage(UUID userId) {
        UserStorageUsageEntity entity = userStorageUsageRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User storage usage not found for user: " + userId));
        
        return UserStorageUsageEntityMapper.INSTANCE.toUserStorageUsage(entity);
    }
}