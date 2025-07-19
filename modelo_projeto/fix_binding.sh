#!/bin/bash
# Corrigir PhotoAdapter.kt
sed -i 's/binding\.imageViewPhoto/viewHolder.itemView.findViewById(R.id.imageViewPhoto)/g' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt
sed -i 's/binding\.imageButtonRemovePhoto/viewHolder.itemView.findViewById(R.id.imageButtonRemovePhoto)/g' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt
sed -i '1a import android.view.View' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt

# Corrigir NovaVistoriaFragment.kt
sed -i 's/binding\.recyclerViewAmbientes/view.findViewById(R.id.recyclerViewAmbientes)/g' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/ui/fragments/NovaVistoriaFragment.kt
sed -i '1a import android.view.View' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/ui/fragments/NovaVistoriaFragment.kt
