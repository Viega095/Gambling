package com.gamblingplugin.utils;

import org.bukkit.entity.Player;

public class GameTutorials {

    public static void sendRouletteTutorial(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l════════ CÓMO JUGAR RULETA ════════");
        player.sendMessage("");
        player.sendMessage("§e1. §7Coloca tu apuesta usando las fichas");
        player.sendMessage("§e2. §7Elige color: §cRojo§7, §8Negro §7o §aVerde");
        player.sendMessage("§e3. §7La ruleta girará y elegirá un número");
        player.sendMessage("");
        player.sendMessage("§6§lPAGOS:");
        player.sendMessage("  §cRojo/§8Negro: §ax2 §7(paga el doble)");
        player.sendMessage("  §aVerde: §ax14 §7(paga 14 veces)");
        player.sendMessage("");
        player.sendMessage("§7§o¡Buena suerte!");
        player.sendMessage("§6§l═══════════════════════════════════");
        player.sendMessage("");
    }

    public static void sendDiceTutorial(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l═════════ CÓMO JUGAR DADOS ═════════");
        player.sendMessage("");
        player.sendMessage("§e1. §7Coloca tu apuesta");
        player.sendMessage("§e2. §7Adivina el número (1-6)");
        player.sendMessage("§e3. §7El dado se lanzará");
        player.sendMessage("");
        player.sendMessage("§6§lPAGOS:");
        player.sendMessage("  §7Número correcto: §ax6 §7(paga 6 veces)");
        player.sendMessage("  §7Número incorrecto: §cPierdes");
        player.sendMessage("");
        player.sendMessage("§7§o¡Apuesta con inteligencia!");
        player.sendMessage("§6§l═══════════════════════════════════");
        player.sendMessage("");
    }

    public static void sendCoinflipTutorial(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l═══════ CÓMO JUGAR COINFLIP ═══════");
        player.sendMessage("");
        player.sendMessage("§e1. §7Coloca tu apuesta");
        player.sendMessage("§e2. §7Elige §6CARA §7o §8CRUZ");
        player.sendMessage("§e3. §7La moneda se lanzará");
        player.sendMessage("");
        player.sendMessage("§6§lPAGOS:");
        player.sendMessage("  §7Acierto: §ax2 §7(duplicas tu apuesta)");
        player.sendMessage("  §7Fallo: §cPierdes todo");
        player.sendMessage("");
        player.sendMessage("§7§o50% de probabilidad!");
        player.sendMessage("§6§l═══════════════════════════════════");
        player.sendMessage("");
    }

    public static void sendMinesTutorial(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l══════════ CÓMO JUGAR MINES ══════════");
        player.sendMessage("");
        player.sendMessage("§e1. §7Coloca tu apuesta inicial");
        player.sendMessage("§e2. §7Elige casillas una por una");
        player.sendMessage("§e3. §7Evita las §c§lMINAS §c💣");
        player.sendMessage("§e4. §7Retírate cuando quieras con §atus ganancias");
        player.sendMessage("");
        player.sendMessage("§6§lMULTIPLICADORES:");
        player.sendMessage("  §7Cada casilla segura §a aumenta tus ganancias");
        player.sendMessage("  §7Una mina = §c§lPIERDES TODO");
        player.sendMessage("");
        player.sendMessage("§7§o¡Cuánto más lejos llegues, más ganas!");
        player.sendMessage("§6§l══════════════════════════════════════");
        player.sendMessage("");
    }

    public static void sendCaseTutorial(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l═════ CÓMO JUGAR CASE OPENING ═════");
        player.sendMessage("");
        player.sendMessage("§e1. §7Paga por abrir una caja");
        player.sendMessage("§e2. §7La caja girará mostrando premios");
        player.sendMessage("§e3. §7Se detendrá en un premio aleatorio");
        player.sendMessage("");
        player.sendMessage("§6§lPREMIOS POSIBLES:");
        player.sendMessage("  §7Común: §fx1.5");
        player.sendMessage("  §aRaro: §ax3");
        player.sendMessage("  §9Épico: §9x5");
        player.sendMessage("  §6Legendario: §6x10");
        player.sendMessage("");
        player.sendMessage("§7§o¡Abre y cruza los dedos!");
        player.sendMessage("§6§l═══════════════════════════════════");
        player.sendMessage("");
    }

    public static void sendPlinkoTutorial(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l════════ CÓMO JUGAR PLINKO ════════");
        player.sendMessage("");
        player.sendMessage("§e1. §7Coloca tu apuesta");
        player.sendMessage("§e2. §7Suelta la bola desde arriba");
        player.sendMessage("§e3. §7Rebotará en clavijas hacia abajo");
        player.sendMessage("§e4. §7Caerá en un multiplicador");
        player.sendMessage("");
        player.sendMessage("§6§lMULTIPLICADORES:");
        player.sendMessage("  §7Centro: §ax2-x5 §7(más común)");
        player.sendMessage("  §7Bordes: §cx0.5 §7o §6x10+ §7(raro)");
        player.sendMessage("");
        player.sendMessage("§7§o¡Pura suerte y física!");
        player.sendMessage("§6§l═══════════════════════════════════");
        player.sendMessage("");
    }

    public static void sendBlackjackTutorial(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l══════ CÓMO JUGAR BLACKJACK ══════");
        player.sendMessage("");
        player.sendMessage("§e1. §7Coloca tu apuesta");
        player.sendMessage("§e2. §7Recibes 2 cartas (el dealer también)");
        player.sendMessage("§e3. §7Intenta llegar a §621 §7sin pasarte");
        player.sendMessage("§e4. §7§lHIT §7= pedir carta, §l STAND §7= plantarte");
        player.sendMessage("§e5. §7§lDOUBLE §7= doblar apuesta y recibir 1 carta");
        player.sendMessage("");
        player.sendMessage("§6§lPAGOS:");
        player.sendMessage("  §7Blackjack (21 con 2 cartas): §ax2.5");
        player.sendMessage("  §7Ganar normal: §ax2");
        player.sendMessage("  §7Empate: §eRecuperas apuesta");
        player.sendMessage("  §7Bust (+21) o perder: §cPierdes");
        player.sendMessage("");
        player.sendMessage("§7§o¡Estrategia y suerte!");
        player.sendMessage("§6§l═══════════════════════════════════");
        player.sendMessage("");
    }

    public static void sendSlotsTutorial(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l═════════ CÓMO JUGAR SLOTS ═════════");
        player.sendMessage("");
        player.sendMessage("§e1. §7Coloca tu apuesta");
        player.sendMessage("§e2. §7Presiona el botón para girar");
        player.sendMessage("§e3. §7Las 3 ruedas girarán");
        player.sendMessage("§e4. §7Ganas si los símbolos coinciden");
        player.sendMessage("");
        player.sendMessage("§6§lPAGOS:");
        player.sendMessage("  §7777: §6x50 §7(JACKPOT!)");
        player.sendMessage("  §73 iguales: §ax10");
        player.sendMessage("  §72 iguales: §ex3");
        player.sendMessage("");
        player.sendMessage("§7§o¡Clásico casino!");
        player.sendMessage("§6§l═══════════════════════════════════");
        player.sendMessage("");
    }
}
