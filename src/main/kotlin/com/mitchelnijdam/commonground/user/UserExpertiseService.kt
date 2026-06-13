package com.mitchelnijdam.commonground.user

import com.mitchelnijdam.commonground.label.Label
import com.mitchelnijdam.commonground.label.LabelRepository
import com.mitchelnijdam.commonground.label.LabelType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserExpertiseService(
    private val userRepository: UserRepository,
    private val labelRepository: LabelRepository,
) {

    /** Replaces the user's expertise selection. Only LANGUAGE labels are valid expertise in v1. */
    @Transactional
    fun replaceExpertise(user: User, labelIds: List<Long>): Set<Label> {
        val labels = labelRepository.findAllById(labelIds.distinct())

        val missing = labelIds.toSet() - labels.map { it.id }.toSet()
        if (missing.isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown label ids: $missing")
        }
        val nonLanguage = labels.filter { it.labelType != LabelType.LANGUAGE }
        if (nonLanguage.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only LANGUAGE labels can be expertise: ${nonLanguage.map { it.name }}",
            )
        }

        val managedUser = userRepository.findById(user.id).orElseThrow()
        managedUser.expertise.clear()
        managedUser.expertise.addAll(labels)
        userRepository.save(managedUser)
        return managedUser.expertise
    }
}
