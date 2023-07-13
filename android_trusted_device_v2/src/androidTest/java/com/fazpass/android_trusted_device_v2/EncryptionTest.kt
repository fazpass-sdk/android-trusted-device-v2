package com.fazpass.android_trusted_device_v2

import android.util.Base64
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fazpass.android_trusted_device_v2.`object`.Coordinate
import com.fazpass.android_trusted_device_v2.`object`.DeviceInfo
import com.fazpass.android_trusted_device_v2.`object`.MetaData
import com.fazpass.android_trusted_device_v2.`object`.MetaDataSerializer
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class EncryptionTest {
    
    companion object {
        // public_2.pub
        private const val encodedPublicKey = "-----BEGIN RSA PUBLIC KEY-----\n" +
                "MIIEIjANBgkqhkiG9w0BAQEFAAOCBA8AMIIECgKCBAEAqrElGFC/hCOFCbwPpE1S\n" +
                "Io1dd8BAQj8EHt7UX69YrBeF9z+HqNKoH0ZaslndC397HLupVCgyk0twr+ClcmAv\n" +
                "Zv1ABBrkG3ybkYshnDRrQRW07aO6p/EzpO+V3y7/jbFLZDxikcC6BhqvFbDrgZx2\n" +
                "0vpWurKX9YLzvgIVzjpKZxUaD6S9CwuWonqwLKNMVxzpORHLYucnWRUCtdttzIkR\n" +
                "AYSQ41AKDdOWEyKv3oiiVPXnDSQauhSHhvlQ8J20uQMV9XhfTBkABY4nJjY2PBKB\n" +
                "flLFvUH0f+mmNt+QFjkyCX7SyRmUtyCEH+Mp/pPsBvRvEwiTMD2asJwFmPi0UMsF\n" +
                "QyHXmynqy4TKyl62zM1aTkg3kgFhAT0yN73fhQ3rKgQZ6uYfQGmYCtt86w6rRG3l\n" +
                "BNlzPseC6uUidCqKSW+DrctweokWM4/+6fncc7haZCL4tDkLqwhK8lLQ1rsPvDcm\n" +
                "dO1KX4FAeFj2uPlXM60KyKKuw8hr70YnQPiMWZ4HwMon5B7kRy5JVlPijCVg8D55\n" +
                "tAsE93fv5mF3CVqdzToKxb11/ZsJRcvoeIfWjwBP4uLFzp3DxLA+6BVikGG8uior\n" +
                "hhoRvthvUNhIgDOlyKJQvXrKiIGWO3v0BV7/f2ZFjdzu0r2qvozFYnBCO/MQuLqB\n" +
                "TPFNUXFnN4TVrNYzSYGeikqYsNQsjj4VtY1NsNcM6Gt5NuCV14LdiPvQD80zEFug\n" +
                "//xE7lhS1NmJIqSeYnOjjaFwubYu+pMvMjo2wDYUZIun/gKXBXVA5KNrX2Df4aTj\n" +
                "DCQZqIqXZaUhzqfgWv7fg5BkDTrUjuC5JXrtZfOhkQ8mN03uHPz62mSisf7tQysU\n" +
                "7fks5l5HN6RluSc7dNGl7BPMpCL9SfM4x58dQOOvNp2T2ndB18EKmwCFAS9Efdu2\n" +
                "H+hkxkIV5/+1ZZ1IoByWXZcCF+NjJiWufvX6Fz7cRsigIrkK6Y38rHhHpO87jdHA\n" +
                "YM/wXrN2N7sAF2Pu62nU8QE7AOGii0YloqiLErmbUbd/M6qzUB1MxE/CJc34DoBU\n" +
                "PLnv4vyZCsx9ukQrUtfGKb1d6njh+80fWUoDimIP40Qy4w6y/AZPoUW+wROPEDr6\n" +
                "bTXIOXq5olOwmY+906e0ILI9SDR2HUjvXjEb7eT6QzgmPD8HqGY9QzmAD4xENm2g\n" +
                "jGZptOfQptvc/0NWAWGB8y7eq4Jms7oe618IBtR85QXVkz67Wip9nQ/Xm+EKD7mV\n" +
                "bUKjDNOrVVjK8SE6QvvS0WL9nmL0qYeQqWEImGO/LeWYxHXs3DTLIVIQ7fo4QdY1\n" +
                "2ichepNq0f9KnmKitBaDjQKkXyPC2IcwDYdEOrLWY792u4C5GeI+he2SQLMKfkkn\n" +
                "0QIDAQAB\n" +
                "-----END RSA PUBLIC KEY-----"

        // private_2.key
        private const val encodedPrivateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIISJwIBAAKCBAEAqrElGFC/hCOFCbwPpE1SIo1dd8BAQj8EHt7UX69YrBeF9z+H\n" +
                "qNKoH0ZaslndC397HLupVCgyk0twr+ClcmAvZv1ABBrkG3ybkYshnDRrQRW07aO6\n" +
                "p/EzpO+V3y7/jbFLZDxikcC6BhqvFbDrgZx20vpWurKX9YLzvgIVzjpKZxUaD6S9\n" +
                "CwuWonqwLKNMVxzpORHLYucnWRUCtdttzIkRAYSQ41AKDdOWEyKv3oiiVPXnDSQa\n" +
                "uhSHhvlQ8J20uQMV9XhfTBkABY4nJjY2PBKBflLFvUH0f+mmNt+QFjkyCX7SyRmU\n" +
                "tyCEH+Mp/pPsBvRvEwiTMD2asJwFmPi0UMsFQyHXmynqy4TKyl62zM1aTkg3kgFh\n" +
                "AT0yN73fhQ3rKgQZ6uYfQGmYCtt86w6rRG3lBNlzPseC6uUidCqKSW+DrctweokW\n" +
                "M4/+6fncc7haZCL4tDkLqwhK8lLQ1rsPvDcmdO1KX4FAeFj2uPlXM60KyKKuw8hr\n" +
                "70YnQPiMWZ4HwMon5B7kRy5JVlPijCVg8D55tAsE93fv5mF3CVqdzToKxb11/ZsJ\n" +
                "RcvoeIfWjwBP4uLFzp3DxLA+6BVikGG8uiorhhoRvthvUNhIgDOlyKJQvXrKiIGW\n" +
                "O3v0BV7/f2ZFjdzu0r2qvozFYnBCO/MQuLqBTPFNUXFnN4TVrNYzSYGeikqYsNQs\n" +
                "jj4VtY1NsNcM6Gt5NuCV14LdiPvQD80zEFug//xE7lhS1NmJIqSeYnOjjaFwubYu\n" +
                "+pMvMjo2wDYUZIun/gKXBXVA5KNrX2Df4aTjDCQZqIqXZaUhzqfgWv7fg5BkDTrU\n" +
                "juC5JXrtZfOhkQ8mN03uHPz62mSisf7tQysU7fks5l5HN6RluSc7dNGl7BPMpCL9\n" +
                "SfM4x58dQOOvNp2T2ndB18EKmwCFAS9Efdu2H+hkxkIV5/+1ZZ1IoByWXZcCF+Nj\n" +
                "JiWufvX6Fz7cRsigIrkK6Y38rHhHpO87jdHAYM/wXrN2N7sAF2Pu62nU8QE7AOGi\n" +
                "i0YloqiLErmbUbd/M6qzUB1MxE/CJc34DoBUPLnv4vyZCsx9ukQrUtfGKb1d6njh\n" +
                "+80fWUoDimIP40Qy4w6y/AZPoUW+wROPEDr6bTXIOXq5olOwmY+906e0ILI9SDR2\n" +
                "HUjvXjEb7eT6QzgmPD8HqGY9QzmAD4xENm2gjGZptOfQptvc/0NWAWGB8y7eq4Jm\n" +
                "s7oe618IBtR85QXVkz67Wip9nQ/Xm+EKD7mVbUKjDNOrVVjK8SE6QvvS0WL9nmL0\n" +
                "qYeQqWEImGO/LeWYxHXs3DTLIVIQ7fo4QdY12ichepNq0f9KnmKitBaDjQKkXyPC\n" +
                "2IcwDYdEOrLWY792u4C5GeI+he2SQLMKfkkn0QIDAQABAoIEAEzunouBCXCjGbkz\n" +
                "e9FP0C8mi/QHQG8bO942DH4757lYuNahWaFroL7H1OUBv7EI+hIPOthlbbYZkGaU\n" +
                "17zfQT4n8oxnGlyRIwTMMo/WyGiDQhLBcsVy3xX9rgzzf9b7C8HXvY130eAicrtS\n" +
                "DDNUs/GPRzvKdtb++KCxC+bxOsuPJTnbxCCQTopYxcK4rxWsQCmqrYKcMI3j/F6+\n" +
                "0ZQYSwPmHe92CbIFpWRmg+HGr3OfiU3Q8gLgcd0RzbtFOUkR7PnAESOZOPtIALkm\n" +
                "Qfd9V8P5xonwiiSEyJd+19BMYxMIiDR/RRnsm+BLYIgPX5ga310yxjQ979Sd3Z/+\n" +
                "qDap6mtBe43BrMOhzEy9t6JDA+q4feNen7wROusItw+fIFjFlHDc20UK0q5CEuFW\n" +
                "sTacWVaAM4MeJRF9yLOW4NEMY8sXIFD6LHl88yW9qfC2LHdpk7KW631gtXbWgK3n\n" +
                "jcYFDV4x/Q1ikg4r+reQGg9+dJfNSvuO/6bOda7A13Ek2tW3x5b3nWe/ouo30Knd\n" +
                "xv1BC1VsigaHlia90wryPqFbAbPyGpX9JIbCyrpYbwnw2BCoqQLg9HVYAmJUESKG\n" +
                "CqvVidxdL5ZWsoU7liHCJXxyO/XKvDIeRfn1ehxZnspq6/puk22EcsqDZAtkEVY3\n" +
                "zVeVJ+Xn9DM664yIYE4QFPmSZERrcj39UIiYb+fztPToKtaT9roq914K/Xtk/3bx\n" +
                "jt5VbqNmxa8Dvuk5pnjVM8JW580spOjSgu96ykhh1aeE8dYE3zSFeBcW9O39wWCa\n" +
                "GVCY1/OKWwSo+S/961xUgGKGqkAea+rfPLEFBSFmTKPBpflglCj3hoHZnDBFKEKR\n" +
                "WkXq+s60GP7NbJUQW8kcAxzYdmiP1K/gI/i7wTGAbXa0Juw1aOhmGOUP0FuVEJ0i\n" +
                "/rdli/3zImAtifpsW461zpQHvAZ069kR8SGBG8ARKz1FYcCyN8KdllLNjaYgv2Rw\n" +
                "FwdxyuWoyclY+469W04eWV9G39MSgly4NwcHspZp2sq3E+fuRzZ+T8w28x9j1rwR\n" +
                "ZDxdRiLqHRvu05lAvaSQP//+QWT3bc8jjZCxr+umePLD0LmAF4dWVMFdfrazgCl3\n" +
                "V7V2V2HD/J4REbT8tUfvj41LNILBWFk/Sa9wDhKkD7JNjN/KpBxtiDTU+iEn+RHM\n" +
                "aP8pClBMnzgwXmy+kk/e/LxyE3UMtdWZEpu3N3RXTi/H88GHjRyO8BKCmxXVC4E/\n" +
                "tu7tvJQQNk/TTgiUrSgjd8gefYS0FrAYini3aCq8ylws9wzU1yWDoNA00ga2o0/k\n" +
                "jlBHyyiDGXpeb9M+cEWGl82CdSgCiUqgnSH/Gx8rTeihMWycx4oa8nhi4pJa9HVf\n" +
                "hKe/IAECggIBANYMGnsLN3KILQJ+K2/10SdEqCJWkzknA3m0F8lGoZKkbECsYF6e\n" +
                "q3wOqz5zKNcWG0iBWUOPp2tblNGR0sDr74KH1Aq/dn7AxAnFqQBrags6MIcRmHGl\n" +
                "y7EiFkOYcd6BEkfom7yfBPQ/WO224PJws0XCzn0mGoImyjNqkPwX6QrAyflzMyxQ\n" +
                "p09Et/nWFAihKI92+ZhZ1D9/EmUG73XVR7Dp/K+DqP4o6sUXd2FXShcTuLtF691c\n" +
                "HGnf+yvYgrPDuCbJkp6dNCohQVM3ezE3Z97GzGne0jxRksQLE+W9DvMbMOM8Qxdd\n" +
                "Zn5DiA5pcPzMunb/zWL92rkQ/tY7PQAwSCBh41ed0g+QKiAq9XUrYU0FkUKIr+5u\n" +
                "uFY2lycNv2CoQkCWamcXb552LGoU+70gblib562fT/LnaSSQW49NGVffRFn8/VfK\n" +
                "u3+3fyLOdgLzuthHs3VrNYMPi3Oo+xFJLvMBA8RRw7IF9Da8cSmvq1R2TTdDkhQH\n" +
                "ITsXLnC5WqmcW3mKmN4K6cGelF1X4rYI25iEyQvae6AeqvrAvondt06bn5PaoYKM\n" +
                "b2v8mtCXC62Rh1OkZ4meRU/aYFtDo4fEri81og/HLeGK4a5y4Eb0nLqFGcCF8lvO\n" +
                "dvqv/P9UymteYEb0RBAYS415dA1/OkJZ00m/QCUfywy2rPF3XLGxBjUBAoICAQDM\n" +
                "JayD7NtDrtyM6/CEMFaRrkITSxy2rwOHA6Czgj2KVnRKcXwkAmHYtYJAykvoJeEA\n" +
                "diUbhLuqkr/UI6LOq4eqLEhnhXY0PTDoGyuO3zNm9SuxqPSgouSFnGXSS2hn7gzr\n" +
                "0rXN5yRlSH0WJdKw1WLiMT8ETjFojDWreyOS2If5fKsfHBkZzllzfSe392xPF4XP\n" +
                "KZ5T/tbvmbEK4WrS6wxQIBpS/7DDQEXvdREJCdyyy4E2wkHaQIBJHucJeLIiZ4aK\n" +
                "EDp1Jt4pcOuxKpr+pFxL7xwQIS52FVVWWZsIO/rAcyjOZy1nKHZKOdXiS9q57gCE\n" +
                "d0Wd1iC4Nj3dFd8bgwn56fIm/2wFzXRTgu5Hl07qpeawG745Euo74yrn4BcVnzfe\n" +
                "7CWqkxyeU0uY/0gq0CP3XmFzpCRX3vQrJJJ6nafb7YI7csgFzNyEh4qfCMaOBgBu\n" +
                "4IJdLnJdDDBbnek03nHuO/9M9JX3/T1LLmpG0Uab8q2B7XbkA3msnWoepKaF6HqE\n" +
                "mo02mGaIXEgEzwcZ5HlKoLPxP/nn6E0apdLILR1xUpeUzZzDH8x+0xm25Xu4BSKd\n" +
                "nbRlvw+zYen6VfzE+e+Hsq8hf9KQHZnG8RwSpO6AX6h/S0Ni2/UjBSp3kcDLvKav\n" +
                "4XQuEv+z/w3q7tsIXcNwwIila5i81OhFih8w7G3i0QKCAgB4u578JFmEKrdono4V\n" +
                "usvcoGdN5xNnu0/EiAaaq1Jbio7c893vwQPtF8ETmpVArOF6xir5ZsACWEfa5avA\n" +
                "0dPpBoBeB5zo5gYtlOY8rFnGN25D4XLhN6lrIlT4j7Y60QbktJmTjQaYVlrsyqib\n" +
                "V2JnSPh7stBk9Ug8NDHPYBU80X2bTJViu6ODnLlBHLV0IKLmOS+T9ac4oY5Ymc7b\n" +
                "4g9sBK9YuKTLp3y06wWVTE0oMyGR0GC4HUqRlZrxIlCm4RCwICapfPZ9hQB51eOQ\n" +
                "4TWffPa0CNEA4Oot1inE/hy+l2m94rHdLfuv4JuPtX3buga7NJI+8f07QhDb+dHw\n" +
                "l0s3qL85HnXr/V8lgdWt32gkr609oCfMD3u+dVBQ4Po/pOip0a32tEtrUDDBLVDJ\n" +
                "Xs+e/2Fwl1nbIxg1XY+nhs9ytyaws3ia2mlkIrZ9hYMfKycK83aQFoa6hDweYwQq\n" +
                "veLTgpEDXTNK6PzVGvmhj8rN+2SWdXCiB1uBHxQ81PAEl4MpTTnolLNBWRdKh1YD\n" +
                "Y3+pycw0+Wmd6W57vdg7n2nT1wftt/Y90sTOXk9ctLYLkviH/0W/ZByoa/7Ju58z\n" +
                "54ZwbooY6DpC67lcUsOagkMdGhJqe6M2kYCe6kGEK+l5ImSf4Qu7IFkNVaJWMptD\n" +
                "7bK1+EBQMFyxsSBfmvoX6EgeAQKCAgBoX76jOJ8mLKngduTigOBR3Vk1CgslL7fK\n" +
                "+MTZ4nXaW7dbNEh3FKy4Ipz0yTkj+PHfzYfNWQCmBh+Ds7CSn8pd7AQcwXSc4sg1\n" +
                "c2bCxkU+l4z+aVahJodV9I9uhFVeBayfiXygSFQoYHZrs2zUMlU7Nlh0uBG01TK/\n" +
                "eNbIRmLG4MGQx0niEslTLaL4AM1kZq9oKmwjn8B+ggKtgHCgY8JI2RD8bHQQb7UR\n" +
                "57WM/HjVBQ0MVSdcEexE9kLDae0RgAKeZgutR/EAGRa33wOdu6TgweCEMqLRhri5\n" +
                "zFhiTmA2suojFUd2XY6VBpre909eDlw/Xce/GfJinIDmnz8Rjt6z4rOG0sKE+PVH\n" +
                "mUKAYdDcUXPviE+qWIxfkBaotL64tE3ZcL0VR9+y5v1YvPRFx2MMHzoN6NcYAgqu\n" +
                "YCVwAVkILZePE3FG3eXE8JOkGdWCWXkxNyIPyCZ6gjfbL5JDEJQJbK1ryAVA5D3J\n" +
                "SXLWwDqskte4/RLzIv+7jEPsZNwqzKCPWYFnb2XExAGiS8LirxiAWkeFyeLYecGq\n" +
                "JSvuYnqSLNsJHlLOxFxGX2HfS9cET8PR+AZ6msPJP/9jmUEB6JNZurallvet7SRp\n" +
                "PW7P7XlvMvkwE+fhDHp5rDN5LJD42XwTol5HDDml6npQKkzcKyw4Yoe6o5dUvDoB\n" +
                "C8xEtCy+cQKCAgBVInDLaWvAkIQi3ihYn7riPBV3nmOqIuhfHzGX+N8tLrIdSijL\n" +
                "FxnpVo5PODh9OehYWJpo3mMSbjoTKLRuzdzWNBbgBwXA0BrhwKgszWahSk1NEe2y\n" +
                "0hpIyAIv8VM+QSBNmd9SoqQknUiA0ltd0+98KvU5dw7zt5fI5tTqgmHZAzhkL0/b\n" +
                "s2+NKcWdFylG9w+5cIQdmrCiYZ8SKOfDoGOg80rM+xGrFMvlpa8czhZOhoIkSCH6\n" +
                "YFUNftFAv74eKEXqqo1f5wK2FC+g0k51ReQjgQlOVsIHnxZiaXlX6jyvVKItNefO\n" +
                "I9hzjXte4wVQI8WIrLzj8M/ziBCooJgHbyyc15U+uGOYfbj/5yj17Bo5qm86TSYQ\n" +
                "SG0zoHrgc/Ayqt3dMDv3MezgIND8/FtDwmrU3a9EcbRHVlM3JLRqX+V5rFZoPFly\n" +
                "j0f6sziDoScCV/AwztgDkIOLqRj7b21ZAUNJQMSYdvC4rUxRodDbNlR10t3IdMjY\n" +
                "hI/8z3Fp9v03LCur4bnkV+y4BvKzlnsfOFnJx2YvhWW0XHbGu+GzCa6Fv6inVs1z\n" +
                "s38H9Q+4ds95NekYJtdJOhU/nujFissgQ3UIhuDNdM8pfaWaGqiLj7ZXydV1LMio\n" +
                "u3yLajTzVOSxxAKYaMbycRKPXtyactljdeDC3mad2U6xVR4H0HizfipOUw==\n" +
                "-----END RSA PRIVATE KEY-----"

        private val metaData = MetaData(
            platform = "android",
            isRooted = false,
            isEmulator = false,
            isVpn = false,
            isCloned = false,
            isScreenMirroring = false,
            isDebuggable = false,
            signatures = listOf("signtr="),
            deviceInfo = DeviceInfo("", "", "", ""),
            simNumbers = listOf("081234567890"),
            simOperators = listOf("Named Operator"),
            coordinate = Coordinate(0.0, 0.0),
            isMockLocation = false,
            packageName = "com.app.PackageName",
            ipAddress = "127.0.0.1",
        )

        private val jsonMetaData: String
            get() {
                val objectMapper = ObjectMapper()
                val module = SimpleModule("MetaDataSerializer", Version(1, 0, 0, null, null, null))
                module.addSerializer(MetaData::class.java, MetaDataSerializer())
                objectMapper.registerModule(module)
                return objectMapper.writeValueAsString(metaData)
            }
    }

    @Test
    fun encryptAndDecryptMetaTest() {
        val encryptedMetaData = encryptMetaData(jsonMetaData)
        val decryptedMetaData = decryptMetaData(encryptedMetaData)
        assertEquals(jsonMetaData, decryptedMetaData)
    }

    private fun encryptMetaData(jsonMetaData: String): String {
        var key = encodedPublicKey
        key = key.replace("-----BEGIN RSA PUBLIC KEY-----", "")
            .replace("-----END RSA PUBLIC KEY-----", "")
            .replace("\n", "").replace("\r", "")
        var publicKey: PublicKey? = null
        try {
            val keySpec = X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT))
            val keyFactory = KeyFactory.getInstance("RSA")
            publicKey = keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Encrypt string JSON with public key
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(jsonMetaData.toByteArray(StandardCharsets.UTF_8))

        // Encode to base64 string then return
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    private fun decryptMetaData(encryptedMetaData: String): String {
        var key = encodedPrivateKey
        key = key.replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\n", "").replace("\r", "")
        var privateKey: PrivateKey? = null
        try {
            val keySpec = PKCS8EncodedKeySpec(Base64.decode(key, Base64.DEFAULT))
            val keyFactory = KeyFactory.getInstance("RSA")
            privateKey = keyFactory.generatePrivate(keySpec)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val decryptedMetaData = Base64.decode(encryptedMetaData, Base64.DEFAULT)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(decryptedMetaData)

        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}