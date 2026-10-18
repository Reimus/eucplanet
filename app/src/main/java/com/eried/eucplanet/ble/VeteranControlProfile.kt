package com.eried.eucplanet.ble

/** Model-specific command mapping; transport, capabilities and UI stay unchanged. */
internal interface VeteranControlProfile {
    fun setLight(on: Boolean): ByteArray
    fun setLightFollowup(on: Boolean): ByteArray?

    companion object {
        fun forModel(model: VeteranModel?): VeteranControlProfile =
            if (model == VeteranModel.NOSFET_AEON ||
                model == VeteranModel.SHERMAN || model == VeteranModel.SHERMAN_MAX
            ) AsciiVeteranControlProfile else DefaultVeteranControlProfile
    }
}

internal object DefaultVeteranControlProfile : VeteranControlProfile {
    override fun setLight(on: Boolean): ByteArray = VeteranCommands.setHighBeam(on)
    override fun setLightFollowup(on: Boolean): ByteArray = VeteranCommands.setHighBeamCompanion(on)
}

internal object AsciiVeteranControlProfile : VeteranControlProfile {
    // Legacy Sherman and Aeon use the single ASCII light write, no companion.
    override fun setLight(on: Boolean): ByteArray = VeteranCommands.setLight(on)
    override fun setLightFollowup(on: Boolean): ByteArray? = null
}
