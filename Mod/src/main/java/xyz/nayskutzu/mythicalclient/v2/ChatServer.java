package xyz.nayskutzu.mythicalclient.v2;

import fi.iki.elonen.NanoHTTPD;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import java.util.Arrays;

public class ChatServer extends NanoHTTPD {
    private static final Gson gson = new Gson();
    private static final Map<String, String> messages = new ConcurrentHashMap<>();
    private static final AtomicLong messageCounter = new AtomicLong(0);
    private static ChatServer instance;
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());
    private static final String PASSWORD = "margareta28"; // Change this to your desired password
    private static final String SESSION_COOKIE = "chat_auth";
    private static final List<String> commandHistory = new ArrayList<>();
    private static final Map<String, String> quickCommands = new ConcurrentHashMap<>();
    private static final int MAX_COMMAND_HISTORY = 50;

    static {
        // Initialize some default quick commands
        quickCommands.put("Party NaysKutzu", "/party invite NaysKutzu");
        quickCommands.put("Party Maria", "/party invite Maria_Int");
        quickCommands.put("Party accept Kutzu", "/party accept NaysKutzu");
        quickCommands.put("Party accept Maria", "/party accept Maria_Int");
        quickCommands.put("Play DUO", "/play bw-duo");
    }

    public ChatServer(int port) {
        super(port);
        instance = this;
        logger.setLevel(Level.ALL);
    }

    public static ChatServer getInstance() {
        return instance;
    }

    private boolean isAuthenticated(IHTTPSession session) {
        String cookie = session.getCookies().read(SESSION_COOKIE);
        return cookie != null && cookie.equals(PASSWORD);
    }

    public void broadcastMessage(String message) {
        try {
            String id = String.valueOf(messageCounter.incrementAndGet());
            messages.put(id, message);
        } catch (Exception e) {
            logger.severe("Error broadcasting message: " + e.getMessage());
        }
    }

    public void sendToMinecraft(String message) {
        try {
            if (Minecraft.getMinecraft().thePlayer != null) {
                if (message.startsWith("/")) {
                    logger.info("Executing command: " + message);
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(message);
                    addToCommandHistory(message);
                } else {
                    logger.info("Sending chat message: " + message);
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(message);
                }
            } else {
                logger.warning("Cannot send message - player is null");
            }
        } catch (Exception e) {
            logger.severe("Error sending message to Minecraft: " + e.getMessage());
        }
    }

    public List<String> getNearbyPlayers() {
        List<String> players = new ArrayList<>();
        try {
            if (Minecraft.getMinecraft().theWorld != null) {
                for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                    if (player != Minecraft.getMinecraft().thePlayer) {
                        double distance = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(player);
                        players.add(String.format("%s (%.1f blocks)", player.getName(), distance));
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Error getting nearby players: " + e.getMessage());
        }
        return players;
    }

    public void addToCommandHistory(String command) {
        if (command.startsWith("/")) {
            commandHistory.add(0, command);
            if (commandHistory.size() > MAX_COMMAND_HISTORY) {
                commandHistory.remove(commandHistory.size() - 1);
            }
        }
    }

    public List<String> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }

    public void addQuickCommand(String alias, String command) {
        quickCommands.put(alias, command);
    }

    public Map<String, String> getQuickCommands() {
        return new HashMap<>(quickCommands);
    }

    public List<String> getCommandSuggestions(String input) {
        List<String> suggestions = new ArrayList<>();
        try {
            if (Minecraft.getMinecraft().thePlayer != null && input.startsWith("/")) {
                String trimmed = input.substring(1);
                String[] args = trimmed.split(" ");
                String commandName = args[0];
                String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
                BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();
                ICommandSender sender = Minecraft.getMinecraft().thePlayer;

                for (ICommand cmd : ClientCommandHandler.instance.getCommands().values()) {
                    if (cmd.getCommandName().startsWith(commandName)) {
                        List<String> completions = cmd.addTabCompletionOptions(sender, commandArgs, pos);
                        if (completions != null)
                            suggestions.addAll(completions);
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Error getting command suggestions: " + e.getMessage());
        }
        return suggestions;
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String uri = session.getUri();
            logger.info("Received request: " + session.getMethod() + " " + uri);

            // Handle login
            if (uri.equals("/login")) {
                if (session.getMethod() == Method.POST) {
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);
                    @SuppressWarnings("deprecation")
                    String password = session.getParms().get("password");

                    if (password != null && password.equals(PASSWORD)) {
                        Response response = newFixedLengthResponse(Response.Status.OK, "text/plain",
                                "Login successful");
                        response.addHeader("Set-Cookie", SESSION_COOKIE + "=" + PASSWORD + "; Path=/; HttpOnly");
                        return response;
                    } else {
                        return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Invalid password");
                    }
                }
                return newFixedLengthResponse(Response.Status.OK, "text/html", getLoginPage());
            }

            // Check authentication for all other routes
            if (!isAuthenticated(session)) {
                return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/html", getLoginPage());
            }

            if (uri.equals("/")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html", getWebInterface());
            } else if (uri.equals("/send")) {
                if (session.getMethod() == Method.POST) {
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);
                    @SuppressWarnings("deprecation")
                    String message = session.getParms().get("message");
                    if (message != null && !message.trim().isEmpty()) {
                        logger.info("Received message to send: " + message);
                        sendToMinecraft(message);
                        String id = String.valueOf(messageCounter.incrementAndGet());
                        messages.put(id, message);
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Message sent: " + message);
                    } else {
                        logger.warning("Received empty message");
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain",
                                "Message cannot be empty");
                    }
                }
            } else if (uri.equals("/messages")) {
                if (session.getMethod() == Method.GET) {
                    @SuppressWarnings("deprecation")
                    String lastId = session.getParms().get("lastId");
                    Map<String, String> newMessages = new HashMap<>();

                    if (lastId != null) {
                        try {
                            long lastIdLong = Long.parseLong(lastId);
                            for (Map.Entry<String, String> entry : messages.entrySet()) {
                                try {
                                    long messageId = Long.parseLong(entry.getKey());
                                    if (messageId > lastIdLong) {
                                        newMessages.put(entry.getKey(), entry.getValue());
                                    }
                                } catch (NumberFormatException e) {
                                    logger.warning("Invalid message ID format: " + entry.getKey());
                                }
                            }
                        } catch (NumberFormatException e) {
                            logger.warning("Invalid lastId format: " + lastId);
                        }
                    } else {
                        newMessages.putAll(messages);
                    }

                    logger.info("Sending " + newMessages.size() + " messages to client");
                    return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(newMessages));
                }
            } else if (uri.equals("/players")) {
                if (session.getMethod() == Method.GET) {
                    List<String> players = getNearbyPlayers();
                    return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(players));
                }
            } else if (uri.equals("/command-history")) {
                if (session.getMethod() == Method.GET) {
                    return newFixedLengthResponse(Response.Status.OK, "application/json",
                            gson.toJson(getCommandHistory()));
                }
            } else if (uri.equals("/quick-commands")) {
                if (session.getMethod() == Method.GET) {
                    return newFixedLengthResponse(Response.Status.OK, "application/json",
                            gson.toJson(getQuickCommands()));
                } else if (session.getMethod() == Method.POST) {
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);
                    @SuppressWarnings("deprecation")
                    String alias = session.getParms().get("alias");
                    @SuppressWarnings("deprecation")
                    String command = session.getParms().get("command");
                    if (alias != null && command != null) {
                        addQuickCommand(alias, command);
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Quick command added");
                    }
                }
            } else if (uri.equals("/suggest")) {
                if (session.getMethod() == Method.GET) {
                    @SuppressWarnings("deprecation")
                    String input = session.getParms().get("input");
                    if (input != null) {
                        List<String> suggestions = getCommandSuggestions(input);
                        return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(suggestions));
                    }
                }
            }

            logger.warning("Not found: " + uri);
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
        } catch (Exception e) {
            logger.severe("Error handling request: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain",
                    "Internal Server Error: " + e.getMessage());
        }
    }

    private String getLoginPage() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<title>Minecraft Chat Login</title>" +
                "<script src=\"https://cdn.tailwindcss.com\"></script>" +
                "<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">"
                +
                "<style>" +
                "body { font-family: 'Inter', sans-serif; }" +
                "@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }"
                +
                ".fade-in { animation: fadeIn 0.3s ease-out; }" +
                "</style>" +
                "</head>" +
                "<body class=\"bg-gradient-to-br from-gray-900 to-gray-800 text-gray-100 min-h-screen flex items-center justify-center p-4\">"
                +
                "<div class=\"bg-gray-800/50 backdrop-blur-sm p-8 rounded-2xl shadow-2xl max-w-md w-full border border-gray-700/50 fade-in\">"
                +
                "<div class=\"text-center mb-8\">" +
                "<h1 class=\"text-3xl font-bold bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent\">Minecraft Chat</h1>"
                +
                "<p class=\"text-gray-400 mt-2\">Enter your password to continue</p>" +
                "</div>" +
                "<form id=\"loginForm\" class=\"space-y-6\">" +
                "<div class=\"space-y-2\">" +
                "<label class=\"block text-sm font-medium text-gray-300\">Password</label>" +
                "<input type=\"password\" id=\"password\" " +
                "class=\"w-full bg-gray-700/50 text-gray-100 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 border border-gray-600/50 transition-all duration-200\" "
                +
                "placeholder=\"Enter your password\">" +
                "</div>" +
                "<button type=\"submit\" " +
                "class=\"w-full bg-gradient-to-r from-indigo-500 to-purple-500 hover:from-indigo-600 hover:to-purple-600 text-white font-semibold py-3 px-4 rounded-xl transition-all duration-200 transform hover:scale-[1.02] active:scale-[0.98]\">"
                +
                "Login" +
                "</button>" +
                "</form>" +
                "<div id=\"error\" class=\"mt-4 text-red-400 hidden text-center\"></div>" +
                "</div>" +
                "<script>" +
                "document.getElementById('loginForm').addEventListener('submit', function(e) {" +
                "e.preventDefault();" +
                "const password = document.getElementById('password').value;" +
                "fetch('/login', {" +
                "method: 'POST'," +
                "headers: {" +
                "'Content-Type': 'application/x-www-form-urlencoded'" +
                "}," +
                "body: 'password=' + encodeURIComponent(password)" +
                "})" +
                ".then(response => {" +
                "if (response.ok) {" +
                "window.location.href = '/';" +
                "} else {" +
                "document.getElementById('error').textContent = 'Invalid password';" +
                "document.getElementById('error').classList.remove('hidden');" +
                "document.getElementById('password').value = '';" +
                "document.getElementById('password').focus();" +
                "}" +
                "});" +
                "});" +
                "document.getElementById('password').focus();" +
                "</script>" +
                "</body>" +
                "</html>";
    }

    private String getWebInterface() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<title>Minecraft Chat Interface</title>" +
                "<script src=\"https://cdn.tailwindcss.com\"></script>" +
                "<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">"
                +
                "<link href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css\" rel=\"stylesheet\">"
                +
                "<style>" +
                "body { font-family: 'Inter', sans-serif; }" +
                "@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }"
                +
                ".fade-in { animation: fadeIn 0.3s ease-out; }" +
                ".minecraft-message { white-space: pre-wrap; }" +
                "::-webkit-scrollbar { width: 8px; }" +
                "::-webkit-scrollbar-track { background: rgba(31, 41, 55, 0.5); border-radius: 4px; }" +
                "::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.5); border-radius: 4px; }" +
                "::-webkit-scrollbar-thumb:hover { background: rgba(99, 102, 241, 0.7); }" +
                "@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }" +
                ".animate-pulse { animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }" +
                "</style>" +
                "</head>" +
                "<body class=\"bg-gradient-to-br from-gray-900 to-gray-800 text-gray-100 min-h-screen\">" +
                "<div class=\"container mx-auto px-4 py-8 max-w-6xl\">" +
                "<div class=\"flex gap-4\">" +
                "<div class=\"w-1/4 space-y-4\">" +
                "<div class=\"bg-gray-800/50 backdrop-blur-sm rounded-2xl shadow-2xl overflow-hidden border border-gray-700/50\">"
                +
                "<div class=\"p-4 border-b border-gray-700/50 flex justify-between items-center\">" +
                "<h2 class=\"text-lg font-semibold\">Nearby Players</h2>" +
                "<button onclick=\"refreshPlayers()\" class=\"text-gray-400 hover:text-indigo-400 transition-colors duration-200\" title=\"Refresh\">"
                +
                "<i class=\"fas fa-sync-alt\"></i>" +
                "</button>" +
                "</div>" +
                "<div id=\"playersList\" class=\"p-4 space-y-2 max-h-[200px] overflow-y-auto\"></div>" +
                "</div>" +
                "<div class=\"bg-gray-800/50 backdrop-blur-sm rounded-2xl shadow-2xl overflow-hidden border border-gray-700/50\">"
                +
                "<div class=\"p-4 border-b border-gray-700/50\">" +
                "<h2 class=\"text-lg font-semibold\">Quick Commands</h2>" +
                "</div>" +
                "<div id=\"quickCommands\" class=\"p-4 space-y-2\"></div>" +
                "<div class=\"p-4 border-t border-gray-700/50\">" +
                "<div class=\"flex gap-2\">" +
                "<input type=\"text\" id=\"quickCommandAlias\" placeholder=\"Alias\" " +
                "class=\"flex-1 bg-gray-700/50 text-gray-100 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/50 border border-gray-600/50\">"
                +
                "<input type=\"text\" id=\"quickCommandValue\" placeholder=\"Command\" " +
                "class=\"flex-1 bg-gray-700/50 text-gray-100 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/50 border border-gray-600/50\">"
                +
                "<button onclick=\"addQuickCommand()\" class=\"bg-indigo-500 hover:bg-indigo-600 text-white px-3 py-2 rounded-xl text-sm\">Add</button>"
                +
                "</div>" +
                "</div>" +
                "</div>" +
                "<div class=\"bg-gray-800/50 backdrop-blur-sm rounded-2xl shadow-2xl overflow-hidden border border-gray-700/50\">"
                +
                "<div class=\"p-4 border-b border-gray-700/50\">" +
                "<h2 class=\"text-lg font-semibold\">Command History</h2>" +
                "</div>" +
                "<div id=\"commandHistory\" class=\"p-4 space-y-2 max-h-[200px] overflow-y-auto\"></div>" +
                "</div>" +
                "</div>" +
                "<div class=\"flex-1\">" +
                "<div class=\"bg-gray-800/50 backdrop-blur-sm rounded-2xl shadow-2xl overflow-hidden border border-gray-700/50\">"
                +
                "<div class=\"p-6 border-b border-gray-700/50 flex justify-between items-center\">" +
                "<div class=\"flex items-center space-x-3\">" +
                "<div id=\"connectionStatus\" class=\"w-3 h-3 rounded-full bg-green-400 animate-pulse\"></div>" +
                "<h1 class=\"text-2xl font-bold bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent\">Minecraft Chat</h1>"
                +
                "</div>" +
                "<div class=\"flex items-center space-x-4\">" +
                "<button onclick=\"toggleTheme()\" class=\"text-gray-400 hover:text-yellow-400 transition-colors duration-200\" title=\"Toggle Theme\">"
                +
                "<i class=\"fas fa-moon\"></i>" +
                "</button>" +
                "<button onclick=\"clearChat()\" class=\"text-gray-400 hover:text-red-400 transition-colors duration-200\" title=\"Clear Chat\">"
                +
                "<i class=\"fas fa-trash\"></i>" +
                "</button>" +
                "<button onclick=\"logout()\" class=\"text-gray-400 hover:text-red-400 transition-colors duration-200\" title=\"Logout\">"
                +
                "<i class=\"fas fa-sign-out-alt\"></i>" +
                "</button>" +
                "</div>" +
                "</div>" +
                "<div id=\"chat\" class=\"h-[600px] overflow-y-auto p-6 space-y-3\"></div>" +
                "<div class=\"p-6 border-t border-gray-700/50\">" +
                "<div class=\"flex items-center gap-3 mb-3\">" +
                "<button onclick=\"toggleEmojiPicker()\" class=\"text-gray-400 hover:text-indigo-400 transition-colors duration-200\" title=\"Emojis\">"
                +
                "<i class=\"far fa-smile\"></i>" +
                "</button>" +
                "<div id=\"emojiPicker\" class=\"hidden absolute bottom-24 bg-gray-800 p-4 rounded-xl shadow-lg border border-gray-700\">"
                +
                "<div class=\"grid grid-cols-8 gap-2\">" +
                "<button onclick=\"insertEmoji('😊')\" class=\"hover:bg-gray-700 p-2 rounded\">😊</button>" +
                "<button onclick=\"insertEmoji('😂')\" class=\"hover:bg-gray-700 p-2 rounded\">😂</button>" +
                "<button onclick=\"insertEmoji('❤️')\" class=\"hover:bg-gray-700 p-2 rounded\">❤️</button>" +
                "<button onclick=\"insertEmoji('👍')\" class=\"hover:bg-gray-700 p-2 rounded\">👍</button>" +
                "<button onclick=\"insertEmoji('🎮')\" class=\"hover:bg-gray-700 p-2 rounded\">🎮</button>" +
                "<button onclick=\"insertEmoji('⚔️')\" class=\"hover:bg-gray-700 p-2 rounded\">⚔️</button>" +
                "<button onclick=\"insertEmoji('🏰')\" class=\"hover:bg-gray-700 p-2 rounded\">🏰</button>" +
                "<button onclick=\"insertEmoji('🌍')\" class=\"hover:bg-gray-700 p-2 rounded\">🌍</button>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "<div class=\"flex gap-3\">" +
                "<div class=\"flex-1 relative\">" +
                "<input type=\"text\" id=\"input\" " +
                "class=\"w-full bg-gray-700/50 text-gray-100 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 border border-gray-600/50 transition-all duration-200\" "
                +
                "placeholder=\"Type a message or command...\">" +
                "<div id=\"suggestions\" class=\"hidden absolute bottom-full left-0 right-0 mb-2 bg-gray-800 rounded-xl shadow-lg border border-gray-700 max-h-48 overflow-y-auto\"></div>"
                +
                "</div>" +
                "<button onclick=\"sendMessage()\" " +
                "class=\"bg-gradient-to-r from-indigo-500 to-purple-500 hover:from-indigo-600 hover:to-purple-600 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-200 transform hover:scale-[1.02] active:scale-[0.98]\">"
                +
                "<i class=\"fas fa-paper-plane mr-2\"></i>Send" +
                "</button>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "<script>" +
                "const chat = document.getElementById('chat');" +
                "const input = document.getElementById('input');" +
                "const connectionStatus = document.getElementById('connectionStatus');" +
                "const playersList = document.getElementById('playersList');" +
                "const commandHistory = document.getElementById('commandHistory');" +
                "const quickCommands = document.getElementById('quickCommands');" +
                "let lastId = null;" +
                "let isPolling = true;" +
                "let retryCount = 0;" +
                "const maxRetries = 3;" +
                "const pollInterval = 1000;" +
                "let pollTimeout = null;" +
                "let isDarkTheme = true;" +
                "let lastScrollPosition = 0;" +
                "let isUserScrolling = false;" +
                "let autoScroll = true;" +
                "let playersPollTimeout = null;" +
                "let commandHistoryPollTimeout = null;" +
                "let quickCommandsPollTimeout = null;" +
                "let suggestionsTimeout = null;" +
                "let selectedSuggestion = -1;" +
                "let currentSuggestions = [];" +
                "function updateConnectionStatus(connected) {" +
                "connectionStatus.className = 'w-3 h-3 rounded-full ' + (connected ? 'bg-green-400' : 'bg-red-400') + ' animate-pulse';"
                +
                "}" +
                "function toggleTheme() {" +
                "isDarkTheme = !isDarkTheme;" +
                "document.body.className = isDarkTheme ? 'bg-gradient-to-br from-gray-900 to-gray-800 text-gray-100 min-h-screen' : 'bg-gradient-to-br from-gray-100 to-gray-200 text-gray-900 min-h-screen';"
                +
                "document.querySelector('.container > div').className = isDarkTheme ? 'bg-gray-800/50 backdrop-blur-sm rounded-2xl shadow-2xl overflow-hidden border border-gray-700/50' : 'bg-white/50 backdrop-blur-sm rounded-2xl shadow-2xl overflow-hidden border border-gray-200/50';"
                +
                "}" +
                "function clearChat() {" +
                "if (confirm('Are you sure you want to clear the chat?')) {" +
                "chat.innerHTML = '';" +
                "lastId = null;" +
                "}" +
                "}" +
                "function toggleEmojiPicker() {" +
                "const picker = document.getElementById('emojiPicker');" +
                "picker.classList.toggle('hidden');" +
                "}" +
                "function insertEmoji(emoji) {" +
                "input.value += emoji;" +
                "input.focus();" +
                "document.getElementById('emojiPicker').classList.add('hidden');" +
                "}" +
                "function formatMessage(message) {" +
                "return message.replace(/§([0-9a-fk-or])/g, (match, code) => {" +
                "switch(code) {" +
                "case '0': return '<span class=\"text-gray-900\">';" +
                "case '1': return '<span class=\"text-blue-900\">';" +
                "case '2': return '<span class=\"text-green-900\">';" +
                "case '3': return '<span class=\"text-cyan-900\">';" +
                "case '4': return '<span class=\"text-red-900\">';" +
                "case '5': return '<span class=\"text-purple-900\">';" +
                "case '6': return '<span class=\"text-yellow-900\">';" +
                "case '7': return '<span class=\"text-gray-400\">';" +
                "case '8': return '<span class=\"text-gray-600\">';" +
                "case '9': return '<span class=\"text-blue-400\">';" +
                "case 'a': return '<span class=\"text-green-400\">';" +
                "case 'b': return '<span class=\"text-cyan-400\">';" +
                "case 'c': return '<span class=\"text-red-400\">';" +
                "case 'd': return '<span class=\"text-purple-400\">';" +
                "case 'e': return '<span class=\"text-yellow-400\">';" +
                "case 'f': return '<span class=\"text-white\">';" +
                "case 'k': return '<span class=\"font-mono\">';" +
                "case 'l': return '<span class=\"font-bold\">';" +
                "case 'm': return '<span class=\"line-through\">';" +
                "case 'n': return '<span class=\"underline\">';" +
                "case 'o': return '<span class=\"italic\">';" +
                "case 'r': return '</span>';" +
                "default: return '';" +
                "}" +
                "});" +
                "}" +
                "function scrollToBottom() {" +
                "if (autoScroll) {" +
                "chat.scrollTop = chat.scrollHeight;" +
                "}" +
                "}" +
                "chat.addEventListener('scroll', function() {" +
                "const isAtBottom = chat.scrollHeight - chat.scrollTop <= chat.clientHeight + 100;" +
                "autoScroll = isAtBottom;" +
                "});" +
                "function pollMessages() {" +
                "if (!isPolling) return;" +
                "fetch('/messages' + (lastId ? '?lastId=' + lastId : ''))" +
                ".then(response => {" +
                "if (!response.ok) {" +
                "if (response.status === 401) {" +
                "window.location.href = '/login';" +
                "return;" +
                "}" +
                "throw new Error('Network response was not ok');" +
                "}" +
                "updateConnectionStatus(true);" +
                "return response.json();" +
                "})" +
                ".then(messages => {" +
                "retryCount = 0;" +
                "Object.entries(messages).forEach(([id, message]) => {" +
                "const messageDiv = document.createElement('div');" +
                "messageDiv.innerHTML = formatMessage(message);" +
                "messageDiv.className = 'fade-in p-4 rounded-xl minecraft-message ' + " +
                "(message.startsWith('/') ? 'bg-purple-900/30' : 'bg-gray-700/30') + ' border border-gray-600/30';" +
                "chat.appendChild(messageDiv);" +
                "scrollToBottom();" +
                "lastId = id;" +
                "});" +
                "})" +
                ".catch(error => {" +
                "console.error('Error polling messages:', error);" +
                "updateConnectionStatus(false);" +
                "retryCount++;" +
                "if (retryCount >= maxRetries) {" +
                "isPolling = false;" +
                "const errorDiv = document.createElement('div');" +
                "errorDiv.textContent = 'Connection lost. Please refresh the page.';" +
                "errorDiv.className = 'p-4 rounded-xl bg-red-900/30 border border-red-600/30 text-center';" +
                "chat.appendChild(errorDiv);" +
                "}" +
                "})" +
                ".finally(() => {" +
                "if (isPolling) {" +
                "pollTimeout = setTimeout(pollMessages, pollInterval);" +
                "}" +
                "});" +
                "}" +
                "function sendMessage() {" +
                "const message = input.value.trim();" +
                "if (message) {" +
                "const formData = new FormData();" +
                "formData.append('message', message);" +
                "fetch('/send', {" +
                "method: 'POST'," +
                "body: formData" +
                "})" +
                ".then(response => {" +
                "if (!response.ok) {" +
                "if (response.status === 401) {" +
                "window.location.href = '/login';" +
                "return;" +
                "}" +
                "throw new Error('Network response was not ok');" +
                "}" +
                "return response.text();" +
                "})" +
                ".then(text => {" +
                "const messageDiv = document.createElement('div');" +
                "messageDiv.innerHTML = formatMessage(text);" +
                "messageDiv.className = 'fade-in p-4 rounded-xl minecraft-message bg-indigo-900/30 border border-indigo-600/30';"
                +
                "chat.appendChild(messageDiv);" +
                "scrollToBottom();" +
                "})" +
                ".catch(error => {" +
                "console.error('Error sending message:', error);" +
                "const errorDiv = document.createElement('div');" +
                "errorDiv.textContent = 'Failed to send message. Please try again.';" +
                "errorDiv.className = 'p-4 rounded-xl bg-red-900/30 border border-red-600/30 text-center';" +
                "chat.appendChild(errorDiv);" +
                "});" +
                "input.value = '';" +
                "}" +
                "}" +
                "function logout() {" +
                "document.cookie = 'chat_auth=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';" +
                "window.location.href = '/login';" +
                "}" +
                "input.addEventListener('keypress', function(e) {" +
                "if (e.key === 'Enter') sendMessage();" +
                "});" +
                "window.addEventListener('beforeunload', function() {" +
                "isPolling = false;" +
                "if (pollTimeout) clearTimeout(pollTimeout);" +
                "if (playersPollTimeout) clearTimeout(playersPollTimeout);" +
                "if (commandHistoryPollTimeout) clearTimeout(commandHistoryPollTimeout);" +
                "if (quickCommandsPollTimeout) clearTimeout(quickCommandsPollTimeout);" +
                "});" +
                "document.addEventListener('click', function(e) {" +
                "const picker = document.getElementById('emojiPicker');" +
                "if (!picker.contains(e.target) && !e.target.matches('button[onclick=\"toggleEmojiPicker()\"]')) {" +
                "picker.classList.add('hidden');" +
                "}" +
                "});" +
                "function refreshPlayers() {" +
                "fetch('/players')" +
                ".then(response => response.json())" +
                ".then(players => {" +
                "playersList.innerHTML = '';" +
                "players.forEach(player => {" +
                "const playerDiv = document.createElement('div');" +
                "playerDiv.className = 'p-2 rounded-lg bg-gray-700/30 border border-gray-600/30 text-sm';" +
                "playerDiv.textContent = player;" +
                "playersList.appendChild(playerDiv);" +
                "});" +
                "});" +
                "}" +
                "function pollPlayers() {" +
                "refreshPlayers();" +
                "playersPollTimeout = setTimeout(pollPlayers, 5000);" +
                "}" +
                "function refreshCommandHistory() {" +
                "fetch('/command-history')" +
                ".then(response => response.json())" +
                ".then(commands => {" +
                "commandHistory.innerHTML = '';" +
                "commands.forEach(command => {" +
                "const commandDiv = document.createElement('div');" +
                "commandDiv.className = 'p-2 rounded-lg bg-gray-700/30 border border-gray-600/30 text-sm cursor-pointer hover:bg-gray-600/30';"
                +
                "commandDiv.textContent = command;" +
                "commandDiv.onclick = () => {" +
                "input.value = command;" +
                "input.focus();" +
                "};" +
                "commandHistory.appendChild(commandDiv);" +
                "});" +
                "});" +
                "}" +
                "function pollCommandHistory() {" +
                "refreshCommandHistory();" +
                "commandHistoryPollTimeout = setTimeout(pollCommandHistory, 2000);" +
                "}" +
                "function refreshQuickCommands() {" +
                "fetch('/quick-commands')" +
                ".then(response => response.json())" +
                ".then(commands => {" +
                "quickCommands.innerHTML = '';" +
                "Object.entries(commands).forEach(([alias, command]) => {" +
                "const commandDiv = document.createElement('div');" +
                "commandDiv.className = 'flex items-center justify-between p-2 rounded-lg bg-gray-700/30 border border-gray-600/30 text-sm';"
                +
                "commandDiv.innerHTML = `<span>${alias}</span><span class=\"text-gray-400\">${command}</span>`;" +
                "commandDiv.onclick = () => {" +
                "input.value = command;" +
                "input.focus();" +
                "};" +
                "quickCommands.appendChild(commandDiv);" +
                "});" +
                "});" +
                "}" +
                "function pollQuickCommands() {" +
                "refreshQuickCommands();" +
                "quickCommandsPollTimeout = setTimeout(pollQuickCommands, 5000);" +
                "}" +
                "function addQuickCommand() {" +
                "const alias = document.getElementById('quickCommandAlias').value.trim();" +
                "const command = document.getElementById('quickCommandValue').value.trim();" +
                "if (alias && command) {" +
                "const formData = new FormData();" +
                "formData.append('alias', alias);" +
                "formData.append('command', command);" +
                "fetch('/quick-commands', {" +
                "method: 'POST'," +
                "body: formData" +
                "})" +
                ".then(response => {" +
                "if (response.ok) {" +
                "document.getElementById('quickCommandAlias').value = '';" +
                "document.getElementById('quickCommandValue').value = '';" +
                "refreshQuickCommands();" +
                "}" +
                "});" +
                "}" +
                "}" +
                "input.addEventListener('input', function() {" +
                "const value = input.value.trim();" +
                "if (value.startsWith('/')) {" +
                "if (suggestionsTimeout) {" +
                "clearTimeout(suggestionsTimeout);" +
                "}" +
                "suggestionsTimeout = setTimeout(() => {" +
                "fetch('/suggest?input=' + encodeURIComponent(value))" +
                ".then(response => response.json())" +
                ".then(suggestions => {" +
                "currentSuggestions = suggestions;" +
                "const suggestionsDiv = document.getElementById('suggestions');" +
                "suggestionsDiv.innerHTML = '';" +
                "if (suggestions.length > 0) {" +
                "suggestionsDiv.classList.remove('hidden');" +
                "suggestions.forEach((suggestion, index) => {" +
                "const div = document.createElement('div');" +
                "div.className = 'p-2 hover:bg-gray-700 cursor-pointer text-sm';" +
                "div.textContent = suggestion;" +
                "div.onclick = () => {" +
                "input.value = suggestion;" +
                "suggestionsDiv.classList.add('hidden');" +
                "input.focus();" +
                "};" +
                "suggestionsDiv.appendChild(div);" +
                "});" +
                "} else {" +
                "suggestionsDiv.classList.add('hidden');" +
                "}" +
                "});" +
                "}, 100);" +
                "} else {" +
                "document.getElementById('suggestions').classList.add('hidden');" +
                "}" +
                "});" +
                "input.addEventListener('keydown', function(e) {" +
                "const suggestionsDiv = document.getElementById('suggestions');" +
                "if (!suggestionsDiv.classList.contains('hidden')) {" +
                "if (e.key === 'ArrowDown') {" +
                "e.preventDefault();" +
                "selectedSuggestion = Math.min(selectedSuggestion + 1, currentSuggestions.length - 1);" +
                "updateSelectedSuggestion();" +
                "} else if (e.key === 'ArrowUp') {" +
                "e.preventDefault();" +
                "selectedSuggestion = Math.max(selectedSuggestion - 1, -1);" +
                "updateSelectedSuggestion();" +
                "} else if (e.key === 'Enter' && selectedSuggestion >= 0) {" +
                "e.preventDefault();" +
                "input.value = currentSuggestions[selectedSuggestion];" +
                "suggestionsDiv.classList.add('hidden');" +
                "selectedSuggestion = -1;" +
                "} else if (e.key === 'Escape') {" +
                "suggestionsDiv.classList.add('hidden');" +
                "selectedSuggestion = -1;" +
                "}" +
                "}" +
                "});" +
                "function updateSelectedSuggestion() {" +
                "const suggestions = suggestionsDiv.children;" +
                "for (let i = 0; i < suggestions.length; i++) {" +
                "suggestions[i].className = 'p-2 hover:bg-gray-700 cursor-pointer text-sm' + (i === selectedSuggestion ? ' bg-gray-700' : '');"
                +
                "}" +
                "}" +
                "document.addEventListener('click', function(e) {" +
                "if (!e.target.closest('#input') && !e.target.closest('#suggestions')) {" +
                "document.getElementById('suggestions').classList.add('hidden');" +
                "}" +
                "});" +
                "pollMessages();" +
                "pollPlayers();" +
                "pollCommandHistory();" +
                "pollQuickCommands();" +
                "</script>" +
                "</body>" +
                "</html>";
    }

    public void start() {
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            logger.info("Chat server started on port " + getListeningPort());
        } catch (IOException e) {
            logger.severe("Failed to start chat server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}