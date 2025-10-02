-- KestrelClient Lua Script Examples
-- These are real scripts that will execute with game API access

-- Example 1: Basic logging and system info
log("=== KestrelClient Script Engine Test ===")
local fps = getFPS()
local ping = getPing()
log("Current FPS: " .. fps)
log("Current Ping: " .. ping .. "ms")

-- Example 2: Memory pattern scanning
log("\n--- Memory Pattern Scanning ---")
local pattern = string.char(0x48, 0x89, 0x5C, 0x24) -- Common x64 pattern
local mask = "xxxx"
local addr = findPattern(pattern, mask)
if addr > 0 then
    log("Found pattern at: 0x" .. string.format("%X", addr))
else
    log("Pattern not found")
end

-- Example 3: Memory reading and writing
log("\n--- Memory Operations ---")
local testAddr = 0x140000000 -- Example address (adjust as needed)
local value = readMemory(testAddr)
if value then
    log("Read value from 0x" .. string.format("%X", testAddr) .. ": " .. value)
    
    -- Try to write back the same value
    local success = writeMemory(testAddr, value)
    if success then
        log("Successfully wrote value back")
    else
        log("Failed to write value")
    end
else
    log("Failed to read memory at 0x" .. string.format("%X", testAddr))
end

-- Example 4: Key detection
log("\n--- Input Detection ---")
if keyPressed("VK_SPACE") then
    log("SPACE key is currently pressed!")
end

if keyPressed("VK_RETURN") then
    log("ENTER key is currently pressed!")
end

-- Example 5: Loop example with timing
log("\n--- Loop Example ---")
for i = 1, 5 do
    log("Loop iteration: " .. i)
    -- In a real scenario, you might want to add a small delay
    -- but for demonstration, we'll just log
end

-- Example 6: Function definition and usage
function calculateDistance(x1, y1, x2, y2)
    local dx = x2 - x1
    local dy = y2 - y1
    return math.sqrt(dx*dx + dy*dy)
end

local dist = calculateDistance(0, 0, 3, 4)
log("Distance from (0,0) to (3,4): " .. dist)

-- Example 7: Advanced memory scanning with multiple patterns
log("\n--- Advanced Pattern Scanning ---")
local patterns = {
    {string.char(0x48, 0x89, 0x5C, 0x24), "xxxx"},
    {string.char(0x48, 0x83, 0xEC, 0x20), "xxxx"},
    {string.char(0x40, 0x53, 0x48, 0x83), "xxxx"}
}

for i, patternInfo in ipairs(patterns) do
    local addr = findPattern(patternInfo[1], patternInfo[2])
    if addr > 0 then
        log("Pattern " .. i .. " found at: 0x" .. string.format("%X", addr))
    else
        log("Pattern " .. i .. " not found")
    end
end

log("\n=== Script execution completed ===")
