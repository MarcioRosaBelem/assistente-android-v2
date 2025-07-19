#!/bin/bash
echo "Primeiras 20 linhas do AmbienteAdapter.kt:"
head -n 20 /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/AmbienteAdapter.kt
echo -e "\nPrimeiras 20 linhas do PhotoAdapter.kt:"
head -n 20 /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt
echo -e "\nLinhas 75-90 do RoomAdapter.kt:"
sed -n '75,90p' /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/RoomAdapter.kt
echo -e "\nPrimeiras 20 linhas do NovaVistoriaFragment.kt:"
head -n 20 /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/ui/fragments/NovaVistoriaFragment.kt
