#!/bin/bash
sed -i 's/requireView()\.findViewById(R\.id\.recyclerViewAmbientes)/binding.recyclerViewAmbientes/g' "/workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/ui/fragments/NovaVistoriaFragment.kt"
