#!/usr/bin/env bash

# Mensaje de inicio
# Hecho originalmente por Manuel Rodriguez, editado por Yen
echo "Bienvenido al script de configuración. ¡Disfrute! :D"

# Detener el script en caso de error
set -e

# Variables configurables (EDITAR ESTAS VARIABLES SEGÚN TUS NECESIDADES)
SWAP_SIZE="2048"  # Tamaño de la memoria swap en MiB
EMAIL="yxgd0002@ce.pucmm.edu.do"  # Correo para Certbot
DOMAIN="bandi-cachivaches.duckdns.org"  # Dominio para el certificado SSL
REPO_DIR="~/I34nd1t"  # Directorio del repositorio clonado
GITREPO= "https://github.com/I34nd1t/P2-AWS3" #Repositorio de Github del proyecto
PROJECT_DIR="P2-AWS"  # Directorio del proyecto dentro del repositorio
APACHE_CONF="seguro.conf"  # Nombre del archivo de configuración de Apache

# Verificar si ya se ha creado y habilitado el archivo swap
if ! grep -q '/swapfile' /etc/fstab; then
    echo "Habilitando la memoria de intercambio."

    # Crear el archivo swap
    sudo dd if=/dev/zero of=/swapfile count=$SWAP_SIZE bs=1MiB
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    sudo cp /etc/fstab /etc/fstab.bak
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
else
    echo "El archivo swap ya está configurado y habilitado."
fi

echo "Instalando y configurando Apache con Virtual Host y SSL"

# Actualizar el sistema e instalar dependencias
sudo apt update && sudo apt -y install apache2 certbot python3-certbot-apache unzip zip

# Habilitar módulos necesarios en Apache
sudo a2enmod ssl
sudo a2enmod proxy
sudo a2enmod proxy_http
sudo a2enmod rewrite

# Instalando SDKMAN y Java
echo "Instalando SDKMAN y Java..."
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Instalar Java 17 (o la versión que necesites)
sdk install java 21.0.3-tem

# Entrar a la carpeta del repositorio
cd $REPO_DIR

# Configurar Git Sparse Checkout (solo clonar el directorio parcial-2)
git clone $GITREPO

# Detener Apache ANTES de Certbot
echo "Deteniendo Apache para configurar Certbot..."
sudo systemctl stop apache2

# Generar certificados SSL con Certbot
echo "Generando certificados SSL para el dominio $DOMAIN..."
sudo certbot certonly --standalone -n --agree-tos -m $EMAIL -d $DOMAIN

# Configurar VirtualHost
echo "Configurando VirtualHost..."
sudo cp $REPO_DIR/$PROJECT_DIR/$APACHE_CONF /etc/apache2/sites-available/
sudo a2ensite $APACHE_CONF

# Iniciar Apache DESPUÉS de configurar Certbot
echo "Iniciando Apache..."
sudo systemctl start apache2

# Moverse al directorio del proyecto
cd $REPO_DIR/$PROJECT_DIR/

# Compilar el proyecto con Gradle
echo "Compilando el proyecto con Gradle..."
chmod +x gradlew
./gradlew shadowjar

# Ejecutar la aplicación en segundo plano
echo "Ejecutando la aplicación..."
java -jar build/libs/app.jar > build/libs/salida.txt 2> build/libs/error.txt &

echo "¡Configuración completada! Apache con Virtual Hosts y SSL está funcionando."
