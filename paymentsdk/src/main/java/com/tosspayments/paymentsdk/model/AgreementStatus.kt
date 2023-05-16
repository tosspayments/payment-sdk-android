package com.tosspayments.paymentsdk.model

import org.json.JSONObject

data class AgreementStatus(
    val agreedRequiredTerms: Boolean,
    val terms: List<AgreementTerm>
) {
    companion object {
        fun fromJson(jsonObject: JSONObject): AgreementStatus {
            val agreedRequiredTerms = jsonObject.getBoolean("agreedRequiredTerms")
            val terms = mutableListOf<AgreementTerm>()

            jsonObject.getJSONArray("terms").let { array ->
                for (index in 0 until array.length() - 1) {
                    array.getJSONObject(index).let { jsonObject ->
                        terms.add(
                            AgreementTerm(
                                id = jsonObject.getString("id"),
                                agreed = jsonObject.getBoolean("agreed"),
                                required = jsonObject.getBoolean("required")
                            )
                        )
                    }
                }
            }

            return AgreementStatus(agreedRequiredTerms, terms)
        }
    }
}

data class AgreementTerm(
    val id: String,
    val agreed: Boolean = false,
    val required: Boolean = false
)
