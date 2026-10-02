package com.gamblingplugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class TutorialBookUtils {

    public static ItemStack createTutorialBook(String gameTitle, List<String> rules) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e📖 Guía & Reglas: §6" + gameTitle);
            meta.setLore(rules);
            book.setItemMeta(meta);
        }
        return book;
    }

    public static ItemStack getBaccaratGuide() {
        return createTutorialBook("Baccarat Punto Banco", Arrays.asList(
                "§7• Apuesta al §9Jugador (1:1)§7, §cBanca (0.95:1)§7 o §eEmpate (8:1)§7.",
                "§7• Las cartas suman puntos (módulo 10).",
                "§7• Figuras valen 0, Ases valen 1, números su valor.",
                "§7• El bando más cercano a 9 puntos gana la mano.",
                "§a✔ 100% interactivo y seguro."
        ));
    }

    public static ItemStack getCrashGuide() {
        return createTutorialBook("Cohete Crash 3D", Arrays.asList(
                "§7• El cohete despega y su multiplicador sube.",
                "§7• Puedes retirar en cualquier momento con el botón verde.",
                "§7• ¡Si el cohete explota antes de retirar, pierdes la apuesta!",
                "§7• Ejemplo: Retirar a 2.05x con $100 da $205 de ganancia.",
                "§e⚡ Usa el botón de Cashout rápido durante el vuelo."
        ));
    }

    public static ItemStack getBlackjackGuide() {
        return createTutorialBook("Blackjack 21", Arrays.asList(
                "§7• Consigue 21 puntos o acércate más que el croupier.",
                "§7• Si superas 21, te pasas y pierdes automáticamente.",
                "§7• As vale 1 u 11 puntos según convenga.",
                "§7• Blackjack natural (As + 10/J/Q/K) paga 3 a 2.",
                "§a✔ Pide carta o plántate según tu estrategia."
        ));
    }

    public static ItemStack getRouletteGuide() {
        return createTutorialBook("Ruleta Europea", Arrays.asList(
                "§7• Apuesta a Rojo, Negro, Verde (0) o números exactos.",
                "§7• Rojo / Negro pagan 2x tu apuesta.",
                "§7• Número exacto (0-36) paga 36x tu apuesta.",
                "§7• La bola gira físicamente en la ruleta 3D."
        ));
    }

    public static ItemStack getMinesGuide() {
        return createTutorialBook("Buscaminas (Mines)", Arrays.asList(
                "§7• Descubre casillas seguras para aumentar el multiplicador.",
                "§7• Puedes retirarte y cobrar tras cada casilla destapada.",
                "§7• Si pisas una mina oculta, pierdes toda la apuesta."
        ));
    }

    public static ItemStack getPlinkoGuide() {
        return createTutorialBook("Plinko 3D", Arrays.asList(
                "§7• Suelta la bola en la cima de la pirámide de clavijas.",
                "§7• La bola rebota aleatoriamente hacia los casilleros.",
                "§7• Los extremos tienen multiplicadores gigantes (hasta 100x)."
        ));
    }

    public static ItemStack getLotteryGuide() {
        return createTutorialBook("Lotería Progresiva", Arrays.asList(
                "§7• Compra todos los boletos que desees por $50 c/u.",
                "§7• El 90% del dinero entra al pozo acumulado.",
                "§7• El sorteo se ejecuta automáticamente cada 30 min.",
                "§a✔ ¡El ganador se lleva todo el pozo acumulado!"
        ));
    }

    public static ItemStack getDiceGuide() {
        return createTutorialBook("Dados 3D (Dice)", Arrays.asList(
                "§7• Apuesta a un número exacto (1-6) o a un rango (Bajo 1-3, Alto 4-6).",
                "§7• Número exacto paga §66x§7 tu apuesta.",
                "§7• Rango Bajo / Alto paga §e1.5x§7 tu apuesta.",
                "§7• ¡El dado rueda físicamente en 3D sobre la mesa!"
        ));
    }

    public static ItemStack getCoinflipGuide() {
        return createTutorialBook("Lanzamiento de Moneda (Coinflip)", Arrays.asList(
                "§7• Elige entre §6Cara§7 o §7Cruz§7.",
                "§7• Acierto duplica tu dinero (§a2x§7).",
                "§7• La moneda salta y gira en el aire con física 3D.",
                "§7• ¡Rápido, emocionante y con 50% de probabilidad real!"
        ));
    }

    public static ItemStack getWheelGuide() {
        return createTutorialBook("Mega Rueda de la Fortuna", Arrays.asList(
                "§7• Haz girar la gran rueda vertical con multiplicadores.",
                "§7• Puedes ganar desde §ax1.5§7 hasta el §d⭐ MEGA PREMIO x50 ⭐§7.",
                "§7• ¡Giro gratuito cada 24 horas para todos los jugadores!",
                "§7• La aguja marca el sector ganador con sonido de ruleta."
        ));
    }

    public static ItemStack getCaseGuide() {
        return createTutorialBook("Apertura de Cajas (Cases)", Arrays.asList(
                "§7• Selecciona la caja de recompensas que desees abrir.",
                "§7• La caja se abre en 3D mostrando los premios rodando.",
                "§7• Premios desde objetos raros, dinero, llaves y cosméticos.",
                "§7• ¡Gran probabilidad de objetos legendarios y netherite!"
        ));
    }

    public static ItemStack getSlotsGuide() {
        return createTutorialBook("Tragamonedas 3D (Slots)", Arrays.asList(
                "§7• Tira de la palanca para hacer girar los 3 rodillos 3D.",
                "§7• 2 símbolos iguales pagan §e1.5x§7 tu apuesta.",
                "§7• 3 símbolos iguales pagan entre §610x§7 y §d50x (Nether Star)§7.",
                "§7• ¡Opción de activar el Pozo Progresivo JackPot!"
        ));
    }

    public static ItemStack getLoungeGuide() {
        return createTutorialBook("VIP Cocktail Bar", Arrays.asList(
                "§7• Ordena cócteles con efectos mágicos y beneficios de suerte.",
                "§7• Otorga velocidad, suerte, visión brillante y cashback en casino.",
                "§6🍸 Las bebidas duran entre 10 y 15 minutos."
        ));
    }
}
