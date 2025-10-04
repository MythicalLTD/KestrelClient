# MythicalClient API Documentation

## Overview
MythicalClient provides a web API server that allows external applications to interact with the mod's features. The server runs on a configurable port (default: 9865) and provides several endpoints for different functionalities.

## Server Configuration
- **Default Port**: 9865 (automatically finds next available port if in use)
- **Protocol**: HTTP
- **Base URL**: `http://localhost:{port}`

## Available Endpoints

### 1. Toggle Mods - `/toggle`
Toggle various mod features on/off.

**Method**: `POST`  
**Content-Type**: `application/x-www-form-urlencoded`

#### Request Format
The request body should contain a `postData` parameter with JSON format:

```
postData={"mod":"MOD_NAME"}
```

#### Available Mod Names
- `BridgeHack` - Bridge building assistance
- `NukeProcess` - Process termination utility
- `PlayerESP` - Player ESP overlay
- `Tracers` - Player tracer lines
- `BedESP` - Bed ESP overlay
- `Trajectories` - Projectile trajectory visualization
- `KillGame` - Shutdown Minecraft
- `ForceOP` - Fake OP message
- `ChestESP` - Chest ESP overlay
- `PlayerHealth` - Player health display
- `TntTimer` - TNT countdown timer
- `NoGUI` - Hide GUI elements
- `NearPlayer` - Nearby player detection
- `FireballDetector` - Fireball detection
- `BowDetector` - Bow detection
- `ResourceESP` - Resource ESP overlay
- `ResourceGroundFinder` - Ground resource finder
- `BanMe` - Ban yourself command

#### Example Requests

**Using cURL:**
```bash
curl -X POST http://localhost:9865/toggle \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "postData={\"mod\":\"PlayerESP\"}"
```

**Using PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:9865/toggle" -Method POST -Body "postData={\"mod\":\"PlayerESP\"}" -ContentType "application/x-www-form-urlencoded"
```

**Using Python:**
```python
import requests

response = requests.post(
    'http://localhost:9865/toggle',
    data={'postData': '{"mod":"PlayerESP"}'},
    headers={'Content-Type': 'application/x-www-form-urlencoded'}
)
print(response.text)
```

**Using JavaScript (Node.js):**
```javascript
const axios = require('axios');

axios.post('http://localhost:9865/toggle', 
    'postData={"mod":"PlayerESP"}',
    {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    }
).then(response => {
    console.log(response.data);
});
```

#### Response Format
**Success Response:**
```
HTTP 200 OK
Updated
```

**Error Responses:**
- `400 Bad Request` - Missing or invalid mod parameter
- `500 Internal Server Error` - Server-side error

### 2. Other Available Endpoints
- `/` - Home page
- `/run` - Execute commands
- `/sendchat` - Send chat messages
- `/friends` - Manage friends list
- `/addfriend` - Add friend
- `/removefriend` - Remove friend
- `/getfriends` - Get friends list

## Error Handling
- Always check the HTTP status code
- Parse error messages from the response body
- Handle network timeouts appropriately
- Ensure the mod is running before making requests

## Security Notes
- The API server runs on localhost only
- No authentication is required (local access only)
- Be cautious when using mods like `KillGame` and `BanMe`

## Troubleshooting
1. **Connection Refused**: Ensure MythicalClient is running and the web server started successfully
2. **Port Issues**: Check the console logs for the actual port number if default port is in use
3. **Invalid Mod Name**: Verify the mod name matches exactly (case-sensitive)
4. **Server Errors**: Check the Minecraft console for detailed error messages

## Example Integration
Here's a complete example of a simple mod toggle script:

```python
import requests
import json

class MythicalClientAPI:
    def __init__(self, port=9865):
        self.base_url = f"http://localhost:{port}"
    
    def toggle_mod(self, mod_name):
        """Toggle a mod on/off"""
        try:
            response = requests.post(
                f"{self.base_url}/toggle",
                data={'postData': json.dumps({"mod": mod_name})},
                headers={'Content-Type': 'application/x-www-form-urlencoded'},
                timeout=5
            )
            return response.status_code == 200
        except requests.RequestException as e:
            print(f"Error toggling mod {mod_name}: {e}")
            return False
    
    def toggle_player_esp(self):
        """Toggle Player ESP"""
        return self.toggle_mod("PlayerESP")
    
    def toggle_tracers(self):
        """Toggle Tracers"""
        return self.toggle_mod("Tracers")

# Usage example
api = MythicalClientAPI()
if api.toggle_player_esp():
    print("Player ESP toggled successfully")
else:
    print("Failed to toggle Player ESP")
```
