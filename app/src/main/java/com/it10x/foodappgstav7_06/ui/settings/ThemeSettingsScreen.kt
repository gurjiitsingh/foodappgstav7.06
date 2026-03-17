import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_06.viewmodel.ThemeViewModel
import com.it10x.foodappgstav7_06.ui.theme.PosThemeMode

@Composable
fun ThemeSettingsScreen(vm: ThemeViewModel = viewModel()) {

    val themeModeString by vm.themeMode.collectAsState()
    val themeMode = PosThemeMode.valueOf(themeModeString)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Theme Settings", style = MaterialTheme.typography.titleLarge)

        Divider()

        Text("Theme Mode", style = MaterialTheme.typography.titleMedium)

        listOf(
            PosThemeMode.AUTO,
            PosThemeMode.LIGHT,
            PosThemeMode.DARK,
            PosThemeMode.GSTA,
            PosThemeMode.SQUARE,
            PosThemeMode.LIGHTSPEED,
            PosThemeMode.TOAST
        ).forEach { mode ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = themeMode == mode,
                    onClick = { vm.setThemeMode(mode) }
                )
                Spacer(Modifier.width(8.dp))
                Text(mode.name)
            }
        }
    }
}

