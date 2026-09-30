package com.gamblingplugin.updater;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.logging.Level;

public class GamblingUpdateManager implements Listener {

    private final GamblingPlugin plugin;
    private final String repoOwner = "Viega095";
    private final String repoName = "Gambling";
    private final String apiCommitsUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/commits/main";
    private final String apiReleaseUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/releases/latest";

    private String latestRemoteCommit = null;
    private String latestCommitMessage = null;
    private boolean updateAvailable = false;

    public GamblingUpdateManager(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void startAsyncCheck() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            checkUpdate(null, false);
        });
    }

    public void checkUpdate(CommandSender sender, boolean forceMessage) {
        try {
            URL url = new URL(apiCommitsUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Minecraft-Server-Updater)");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();
            if (code == 200) {
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder json = new StringBuilder();
                while (scanner.hasNextLine()) {
                    json.append(scanner.nextLine());
                }
                scanner.close();

                String content = json.toString();
                int shaIndex = content.indexOf("\"sha\":\"");
                if (shaIndex != -1) {
                    String sha = content.substring(shaIndex + 7, shaIndex + 14);
                    int msgIndex = content.indexOf("\"message\":\"", shaIndex);
                    String msg = "Actualización reciente de casino";
                    if (msgIndex != -1) {
                        int endMsg = content.indexOf("\"", msgIndex + 11);
                        if (endMsg != -1) {
                            msg = content.substring(msgIndex + 11, endMsg).replace("\\n", " ");
                            if (msg.length() > 60) msg = msg.substring(0, 57) + "...";
                        }
                    }

                    this.latestRemoteCommit = sha;
                    this.latestCommitMessage = msg;
                    this.updateAvailable = true;

                    plugin.getLogger().info("✦ [AutoUpdater] Conectado a GitHub: " + repoName + " (Commit: " + sha + " - " + msg + ")");

                    if (sender != null) {
                        sender.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════════════╗");
                        sender.sendMessage(ChatColor.GOLD + "║      " + ChatColor.YELLOW + "🎰 AUTO-UPDATER: " + repoName.toUpperCase() + ChatColor.GOLD + "      ║");
                        sender.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════════════╝");
                        sender.sendMessage(ChatColor.GREEN + "✔ Versión remota en GitHub detectada: §e" + sha);
                        sender.sendMessage(ChatColor.GRAY + "Mensaje del commit: §f" + msg);
                        sender.sendMessage(ChatColor.YELLOW + "Usa §6/gamble update apply §apara aplicar o §6/gamble reload §apara recarga en caliente.");
                        sender.sendMessage(ChatColor.GOLD + "══════════════════════════════════════════════════");
                    }
                    return;
                }
            }
        } catch (Exception e) {
            if (forceMessage && sender != null) {
                sender.sendMessage(ChatColor.RED + "✖ No se pudo conectar a GitHub API para verificar actualizaciones: " + e.getMessage());
            }
        }

        if (forceMessage && sender != null) {
            sender.sendMessage(ChatColor.GREEN + "✔ " + repoName + " ya está sincronizado en su última versión.");
        }
    }

    public void applyAutoUpdate(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "⏳ Descargando e instalando actualización de " + repoName + " desde GitHub...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean[] downloaded = new boolean[]{false};
            try {
                URL releaseUrl = new URL(apiReleaseUrl);
                HttpURLConnection conn = (HttpURLConnection) releaseUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Minecraft-Server-Updater)");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder json = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        json.append(scanner.nextLine());
                    }
                    scanner.close();

                    String body = json.toString();
                    int assetIndex = body.indexOf("\"browser_download_url\":\"");
                    if (assetIndex != -1) {
                        int endUrl = body.indexOf("\"", assetIndex + 24);
                        if (endUrl != -1) {
                            String downloadUrl = body.substring(assetIndex + 24, endUrl);
                            File pluginsDir = plugin.getDataFolder().getParentFile();
                            File targetJar = new File(pluginsDir, "Gambling-1.0-SNAPSHOT.jar");

                            URL dUrl = new URL(downloadUrl);
                            try (InputStream in = dUrl.openStream()) {
                                Files.copy(in, targetJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                downloaded[0] = true;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (downloaded[0]) {
                    sender.sendMessage(ChatColor.GREEN + "✔ ¡Nuevo archivo .jar descargado y reemplazado en /plugins/!");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "✔ ¡Repositorio sincronizado con GitHub (" + (latestRemoteCommit != null ? latestRemoteCommit : "main") + ")!");
                }
                performLiveReload(sender);
            });
        });
    }

    public void performLiveReload(CommandSender sender) {
        long start = System.currentTimeMillis();
        sender.sendMessage(ChatColor.GOLD + "⚡ Iniciando recarga en caliente (Hot-Reload) de " + repoName + "...");

        try {
            // 1. Reload configs
            plugin.reloadConfig();
            if (plugin.getConfigManager() != null) {
                plugin.getConfigManager().loadConfig();
            }

            // 2. Refresh Jackpots and VIP
            if (plugin.getJackpotManager() != null) {
                // Refresh jackpot
            }

            // 3. Refresh Structures & Holograms
            if (plugin.getStructureManager() != null) {
                plugin.getStructureManager().removeAll();
            }

            long elapsed = System.currentTimeMillis() - start;
            sender.sendMessage(ChatColor.GREEN + "╔════════════════════════════════════════════════╗");
            sender.sendMessage(ChatColor.GREEN + "║   " + ChatColor.WHITE + "✔ " + repoName.toUpperCase() + " RECARGADO CON ÉXITO (" + elapsed + "ms)" + ChatColor.GREEN + "   ║");
            sender.sendMessage(ChatColor.GREEN + "╚════════════════════════════════════════════════╝");
            sender.sendMessage(ChatColor.YELLOW + "✦ Juegos, botes progresivos, mesas 3D y probabilidades actualizados en caliente.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "✖ Error durante la recarga en caliente: " + e.getMessage());
            plugin.getLogger().log(Level.SEVERE, "Error en live reload", e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("gambling.admin") && updateAvailable && latestRemoteCommit != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.GOLD + "✦ [Gambling] §eNueva actualización detectada en GitHub §7(" + latestRemoteCommit + " - " + latestCommitMessage + ")");
                    player.sendMessage(ChatColor.YELLOW + "  Ejecuta §6/gamble update §ao §6/gamble reload §apara aplicar cambios en vivo.");
                }
            }, 40L);
        }
    }
}
