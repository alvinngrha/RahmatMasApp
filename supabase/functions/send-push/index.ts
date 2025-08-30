import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { GoogleAuth } from "npm:google-auth-library@8.9.0";
serve(async (req)=>{
  try {
    const { userId, title, body } = await req.json();
    const supabaseUrl = Deno.env.get("https://awhyvidcoelagcgwkqks.supabase.co") ?? "";
    const serviceRoleKey = Deno.env.get("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF3aHl2aWRjb2VsYWdjZ3drcWtzIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1MjIxMzQyNSwiZXhwIjoyMDY3Nzg5NDI1fQ.YtyrUXDwOPTlAZrV5xvxATj8wmZxNZ8DwZXkwyCqsS4") ?? "";
    const projectId = Deno.env.get("sirahmatmas-app") ?? "";
    const serviceAccountJson = Deno.env.get("sirahmatmas-app-firebase-adminsdk-fbsvc-95eefcab8c.json") ?? "{}";
    const serviceAccount = JSON.parse(serviceAccountJson);
    const supabase = createClient(supabaseUrl, serviceRoleKey);
    const { data: tokens, error } = await supabase.from("device_tokens").select("token").eq("user_id", userId);
    if (error) {
      throw error;
    }
    const auth = new GoogleAuth({
      credentials: serviceAccount,
      scopes: [
        "https://www.googleapis.com/auth/firebase.messaging"
      ]
    });
    const client = await auth.getClient();
    const accessToken = await client.getAccessToken();
    await Promise.all(tokens?.map(async ({ token })=>{
      await fetch(`https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`
        },
        body: JSON.stringify({
          message: {
            token,
            notification: {
              title,
              body
            }
          }
        })
      });
    }) ?? []);
    return new Response(JSON.stringify({
      success: true
    }), {
      headers: {
        "Content-Type": "application/json"
      }
    });
  } catch (err) {
    console.error(err);
    return new Response(JSON.stringify({
      error: err.message
    }), {
      status: 500,
      headers: {
        "Content-Type": "application/json"
      }
    });
  }
});
