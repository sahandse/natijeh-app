package com.natijeh.ui.screens

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import ang]÷ç»h‘éì¶»§q«^wVÖâ€¢ÖöF–f–W"ÒÖöF–f–W ¢æf–ÆÄÖ…6—¦R‚¢çFF–ærƒbæG’À¢fW'F–6Ä'&ævVÖVçBÒ'&ævVÖVçBç76VD'’ƒ"æG¢’°¢Ö÷&UF–ÆR€¢F—FÆRÒ-˜M¸Íªş(Í˜}Šr"À¢7V'F—FÆRÒ-ŠÍŠı˜˜MˆÂªı˜M‹-˜mŠ}˜b˜‚Š‹˜mŠ}˜]˜r˜}˜Š­˜r"À¢–6öâÒ–6öç2äFVfVÇBå7F"À¢öä6Æ–6²Ò²öå6VÆV7B‚&ÆVwVW2"’Ğ¢¢Ö÷&UF–ÆR€¢F—FÆRÒ-Š}ŠíŠŠ}‹"À¢7V'F—FÆRÒ-˜˜Š­ŠŠ}˜BŠıŠ}Ší˜M¸Â˜‚ŠíŠ}‹ŠÍ¸ÂŠ}‹"˜‹‹-‹B»2"À¢–6öâÒ–6öç2äWFôÖ—'&÷&VBäf–ÆÆVBäÆ—7BÀ¢öä6Æ–6²Ò²öå6VÆV7B‚&æWw2"’Ğ¢¢Ö÷&UF–ÆR€¢F—FÆRÒ-‹˜MŠ}˜-˜~(Í˜]˜mŠı¸Î(Í˜}Šr"À¢7V'F—FÆRÒ-Š­¸Í˜^(Í˜}Š}ˆÂ˜M¸Íªş(Í˜}Šr˜‚ŠŠ}‹-¸Î(Í˜}Š}¸Â˜]ŠİŠ˜Š‚"À¢–6öâÒ–6öç2äFVfVÇBäff÷&—FRÀ¢öä6Æ–6²Ò²öå6VÆV7B‚&ff÷&—FW2"’Ğ¢¢Ğ§Ğ ¤6ö×÷6&ÆP§&—fFRgVâÖ÷&UF–ÆR‡F—FÆS¢7G&–ærÂ7V'F—FÆS¢7G&–ærÂ–6öã¢–ÖvUfV7F÷"Âöä6Æ–6³¢‚’ÓâVæ—B’°¢6&B€¢6†RÒ&÷VæFVD6÷&æW%6†Rƒ#"æG’À¢6öÆ÷'2Ò6&DFVfVÇG2æ6&D6öÆ÷'2†6öçF–æW$6öÆ÷"ÒÖFW&–ÅF†VÖRæ6öÆ÷%66†VÖRç7W&f6R’À¢VÆWfF–öâÒæF–¦V„6&DVÆWfF–öâ‚’À¢ÖöF–f–W"ÒÖöF–f–W"æf–ÆÄÖ…v–GF‚‚’æ6Æ–6¶&ÆR†öä6Æ–6²Òöä6Æ–6²¢’°¢&÷r€¢ÖöF–f–W"ÒÖöF–f–W"çFF–ærƒ‚æG’À¢fW'F–6ÄÆ–væÖVçBÒÆ–væÖVçBä6VçFW%fW'F–6ÆÇ’À¢†÷&—¦öçFÄ'&ævVÖVçBÒ'&ævVÖVçBç76VD'’ƒbæG¢’°¢&÷‚€¢ÖöF–f–W"ÒÖöF–f–W ¢ç6—¦RƒC‚æG¢æ6Æ—…&÷VæFVD6÷&æW%6†RƒBæG’¢æ&6¶w&÷VæB„ÖFW&–ÅF†VÖRæ6öÆ÷%66†VÖRç&–Ö'’æ6÷’†Ç†Òã&b’’À¢6öçFVçDÆ–væÖVçBÒÆ–væÖVçBä6VçFW ¢’°¢–6öâ†–6öâÂ6öçFVçDFW67&—F–öâÒçVÆÂÂF–çBÒÖFW&–ÅF†VÖRæ6öÆ÷%66†VÖRç&–Ö'’¢Ğ¢6öÇVÖâ†ÖöF–f–W"ÒÖöF–f–W"çvV–v‡Bƒb’ÂfW'F–6Ä'&ævVÖVçBÒ'&ævVÖVçBç76VD'’ƒBæG’’°¢FW‡B‡F—FÆRÂföçEvV–v‡BÒföçEvV–v‡Bä&öÆBÂ7G–ÆRÒÖFW&–ÅF†VÖRçG—öw&‡’çF—FÆTÖVF—VÒ¢FW‡B‡7V'F—FÆRÂ6öÆ÷"ÒÖFW&–ÅF†VÖRæ6öÆ÷%66†VÖRæöå7W&f6Uf&–çBÂ7G–ÆRÒÖFW&–ÅF†VÖRçG—öw&‡’æ&öG•6ÖÆÂ¢Ğ¢Ğ¢Ğ§Ğ ¤6ö×÷6&ÆP¦gVâV×G•7FFR†ÖW76vS¢7G&–ær’°¢&÷‚†ÖöF–f–W"ÒÖöF–f–W"æf–ÆÄÖ…6—¦R‚’çFF–ærƒ3"æG’Â6öçFVçDÆ–væÖVçBÒÆ–væÖVçBä6VçFW"’°¢6öÇVÖâ††÷&—¦öçFÄÆ–væÖVçBÒÆ–væÖVçBä6VçFW$†÷&—¦öçFÆÇ’ÂfW'F–6Ä'&ævVÖVçBÒ'&ævVÖVçBç76VD'’ƒbæG’’°¢–6öâ†–ÖvUfV7F÷"Ò–6öç2äFVfVÇBä–æfòÂ6öçFVçDFW67&—F–öâÒçVÆÂÂF–çBÒÖFW&–ÅF†VÖRæ6öÆ÷%66†VÖRæöå7W&f6Uf&–çBÂÖöF–f–W"ÒÖöF–f–W"ç6—¦RƒcBæG’¢FW‡B‡FW‡BÒÖW76vRÂ6öÆ÷"ÒÖFW&–ÅF†VÖRæ6öÆ÷%66†VÖRæöå7W&f6Uf&–çBÂ7G–ÆRÒÖFW&–ÅF†VÖRçG—öw&‡’æ&öG”Æ&vRÂFW‡DÆ–vâÒFW‡DÆ–vâä6VçFW"¢Ğ¢Ğ§Ğ ¤6ö×÷6&ÆP¦gVâÆöF–æu7FFR‚’°¢&÷‚†ÖöF–f–W"ÒÖöF–f–W"æf–ÆÄÖ…6—¦R‚’Â6öçFVçDÆ–væÖVçBÒÆ–væÖVçBä6VçFW"’°¢6öÇVÖâ††÷&—¦öçFÄÆ–væÖVçBÒÆ–væÖVçBä6VçFW$†÷&—¦öçFÆÇ’ÂfW'F–6Ä'&ævVÖVçBÒ'&ævVÖVçBç76VD'’ƒ"æG’’°¢6—&7VÆ%&öw&W74–æF–6F÷"†6öÆ÷"ÒÖFW&–ÅF†VÖRæ6öÆ÷%66†VÖRç&–Ö'’¢FW‡B‚-Šı‹ŠİŠ}˜BŠı‹¸ÍŠ}˜Š¢˜mŠ­Š}¸ÍŠÂ‹-˜mŠı˜râââ"Â6öÆ÷"ÒÖFW&–ÅF†VÖRæ6öÆ÷%66†VÖRæöå7W&f6Uf&–çBÂ7G–ÆRÒÖFW&–ÅF†VÖRçG—öw&‡’æ&öG”ÖVF—VÒ¢Ğ¢Ğ§Ğ 