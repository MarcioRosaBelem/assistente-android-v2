#!/bin/bash
# Corrigir AmbienteAdapter.kt
sed -i '1i import com.vistoria.app.util.loadImageFromAsset' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/AmbienteAdapter.kt

# Corrigir PhotoAdapter.kt
sed -i '1i import com.vistoria.app.util.loadImageFromPath' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt
sed -i 's/imageViewPhoto/binding.imageViewPhoto/g' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt
sed -i 's/imageButtonRemovePhoto/binding.imageButtonRemovePhoto/g' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt

# Corrigir CameraFragment.kt e PhotoPreviewFragment.kt
sed -i '1i import com.vistoria.app.R' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/ui/fragments/CameraFragment.kt
sed -i '1i import com.vistoria.app.R' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/ui/fragments/PhotoPreviewFragment.kt

# Corrigir NovaVistoriaFragment.kt (assumindo View Binding)
sed -i 's/recyclerViewAmbientes/binding.recyclerViewAmbientes/g' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/ui/fragments/NovaVistoriaFragment.kt

# Corrigir RoomDescriptionFragment.kt
sed -i '1i import com.vistoria.app.ui.fragments.RoomDescriptionFragmentArgs' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/ui/fragments/RoomDescriptionFragment.kt
