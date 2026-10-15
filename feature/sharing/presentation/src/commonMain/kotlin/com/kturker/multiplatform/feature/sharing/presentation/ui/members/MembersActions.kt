package com.kturker.multiplatform.feature.sharing.presentation.ui.members

import com.kturker.multiplatform.core.api.Member
import com.kturker.multiplatform.core.api.Role

internal interface MembersActions {
    fun onBack()
    fun retry()

    /** Opens the role picker for a new invite link. */
    fun startShare()
    fun dismissShare()

    /** Makes an invite link for [role] and hands it to the share sheet. */
    fun share(role: Role)
    fun changeRole(
        member: Member,
        role: Role
    )
    fun requestRemove(member: Member)
    fun confirmRemove()
    fun dismissRemove()
}
