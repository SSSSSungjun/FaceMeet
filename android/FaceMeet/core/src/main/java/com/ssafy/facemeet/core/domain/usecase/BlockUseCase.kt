package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.domain.model.Block
import com.ssafy.facemeet.core.domain.repository.BlockRepository
import javax.inject.Inject

class PostBlockUserUseCase @Inject constructor(
    private val blockRepository: BlockRepository
) {
    suspend operator fun invoke(userId: Long): Result<Unit> {
        return blockRepository.postBlockUser(userId)
    }
}

class GetBlockListUseCase @Inject constructor(
    private val blockRepository: BlockRepository
) {
    suspend operator fun invoke(): Result<List<Block>> {
        return blockRepository.getBlockList()
    }
}

class DeleteBlockUserUseCase @Inject constructor(
    private val blockRepository: BlockRepository
) {
    suspend operator fun invoke(blockedId: Long): Result<Unit> {
        return blockRepository.deleteBlockUser(blockedId)
    }
}