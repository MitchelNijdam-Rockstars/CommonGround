package com.mitchelnijdam.commonground.label

import org.springframework.data.jpa.repository.JpaRepository

interface LabelRepository : JpaRepository<Label, Long> {
    fun findByLabelType(labelType: LabelType): List<Label>
    fun findByNameIgnoreCase(name: String): Label?
}
