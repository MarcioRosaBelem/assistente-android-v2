#!/bin/bash
sed -i 's/private val viewHolder\.itemView\.findViewById(R\.id\.imageViewPhoto): ImageView = itemView\.findViewById(R\.id\.viewHolder\.itemView\.findViewById(R\.id\.imageViewPhoto))/val imageViewPhoto: ImageView = itemView.findViewById(R.id.imageViewPhoto)/g' "/workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt"
sed -i 's/holder\.itemView\.findViewById(R\.id\.imageViewPhoto)/imageViewPhoto/g' "/workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt"
sed -i 's/holder\.itemView\.findViewById(R\.id\.imageButtonRemovePhoto)/itemView.findViewById(R.id.imageButtonRemovePhoto)/g' "/workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt"
sed -i 's/loadImageFromPath(imageViewPhoto)/imageViewPhoto.loadImageFromPath/g' "/workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt"
sed -i '/holder\.itemView\.findViewById<ImageView>(R\.id\.imageViewPhoto)\.loadImageFromPath/d' "/workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt"
