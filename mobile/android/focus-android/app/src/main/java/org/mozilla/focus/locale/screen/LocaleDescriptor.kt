/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.locale.screen

import android.text.TextUtils
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.focus.locale.Locales
import java.text.Collator
import java.util.Locale

class LocaleDescriptor(private val localeTag: String) : Comparable<LocaleDescriptor> {

    private val languageCodeAndNameMap: HashMap<String, String> = HashMap()
    private var nativeName: String? = null

    init {
        fillLanguageCodeAndNameMap()
        setupLocaleDescriptor()
    }

    private fun fillLanguageCodeAndNameMap() {
        // Only ICU 57 actually contains the Asturian name for Asturian, even Android 7.1 is still
        // shipping with ICU 56, so we need to override the Asturian name (otherwise displayName will
        // be the current locales version of Asturian, see:
        // https://github.com/mozilla-mobile/focus-android/issues/634#issuecomment-303886118
        languageCodeAndNameMap["ast"] = "Asturianu"
        // On an Android 8.0 device those languages are not known and we need to add the names
        // manually. Loading the resources at runtime works without problems though.
        languageCodeAndNameMap["ace"] = "Acehnese"
        languageCodeAndNameMap["an"] = "Aragonés"
        languageCodeAndNameMap["anp"] = "अंगिका"
        languageCodeAndNameMap["ay"] = "Aimara"
        languageCodeAndNameMap["cak"] = "Kaqchikel"
        languageCodeAndNameMap["co"] = "Corsu"
        languageCodeAndNameMap["hus"] = "Tének"
        languageCodeAndNameMap["ia"] = "Interlingua"
        languageCodeAndNameMap["ixl"] = "Ixil"
        languageCodeAndNameMap["jv"] = "Basa Jawa"
        languageCodeAndNameMap["meh"] = "Tu'un Savi Yucuhiti"
        languageCodeAndNameMap["mix"] = "Tu'un savi"
        languageCodeAndNameMap["nv"] = "Navajo"
        languageCodeAndNameMap["oc"] = "occitan"
        languageCodeAndNameMap["pai"] = "Paa ipai"
        languageCodeAndNameMap["ppl"] = "Náhuat Pipil"
        languageCodeAndNameMap["quc"] = "K'iche'"
        languageCodeAndNameMap["quy"] = "Chanka Qhichwa"
        languageCodeAndNameMap["skr"] = "سرائیکی"
        languageCodeAndNameMap["sn"] = "ChiShona"
        languageCodeAndNameMap["su"] = "Basa Sunda"
        languageCodeAndNameMap["trs"] = "Triqui"
        languageCodeAndNameMap["tsz"] = "P'urhepecha"
        languageCodeAndNameMap["tt"] = "татарча"
        languageCodeAndNameMap["wo"] = "Wolof"
        languageCodeAndNameMap["zam"] = "DíɁztè"
        languageCodeAndNameMap["zh-CN"] = "中文 (中国大陆)"
    }

    private fun setupLocaleDescriptor() {
        val locale = Locales.parseLocaleCode(localeTag)
        val displayName: String? = getDisplayName(locale)

        if (TextUtils.isEmpty(displayName)) {
            // There's nothing sane we can do.
            Logger.error("Display name is empty. Using $locale")
            nativeName = locale.toString()
            return
        }

        val directionality = Character.getDirectionality(displayName!![0])
        if (directionality == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
            var firstLetter = displayName.substring(0, 1)

            // Android OS creates an instance of Transliterator to convert the first letter
            // of the Greek locale. See CaseMapper.toUpperCase(Locale locale, String s, int count)
            // Since it's already in upper case, we don't need it
            if (!Character.isUpperCase(firstLetter[0])) {
                firstLetter = firstLetter.uppercase(locale)
            }
            nativeName = firstLetter + displayName.substring(1)
            return
        }
        nativeName = displayName
    }

    private fun getDisplayName(locale: Locale): String? {
        return when {
            languageCodeAndNameMap.containsKey(locale.language) -> {
                languageCodeAndNameMap[locale.language]
            }
            languageCodeAndNameMap.containsKey(locale.toLanguageTag()) -> {
                languageCodeAndNameMap[locale.toLanguageTag()]
            }
            else -> {
                locale.getDisplayName(locale)
            }
        }
    }

    fun getTag(): String {
        return localeTag
    }

    fun getNativeName(): String? {
        return nativeName
    }

    override fun hashCode(): Int {
        return localeTag.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is LocaleDescriptor && compareTo(other) == 0
    }

    override operator fun compareTo(other: LocaleDescriptor): Int {
        // We sort by name, so we use Collator.
        return COLLATOR.compare(nativeName, other.nativeName)
    }

    companion object {
        private val COLLATOR = Collator.getInstance(Locale.US)
    }
}
