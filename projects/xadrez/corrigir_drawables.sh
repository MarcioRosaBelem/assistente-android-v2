#!/bin/bash

echo "Corrigindo referências de drawables..."

sed -i 's/@drawable\/sala/@drawable\/img_sala/g' app/src/main/res/layout/activity_main.xml
sed -i 's/@drawable\/cozinha/@drawable\/img_cozinha/g' app/src/main/res/layout/activity_main.xml
sed -i 's/@drawable\/quarto/@drawable\/img_quarto/g' app/src/main/res/layout/activity_main.xml
sed -i 's/@drawable\/banheiro/@drawable\/img_banheiro/g' app/src/main/res/layout/activity_main.xml
sed -i 's/@drawable\/area_servico/@drawable\/img_area_servico/g' app/src/main/res/layout/activity_main.xml

echo "Feito!"
