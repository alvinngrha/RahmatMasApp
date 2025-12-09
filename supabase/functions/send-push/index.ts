import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { GoogleAuth } from "npm:google-auth-library@8.9.0";
serve(async (req)=>{
  try {
    const { userId, title, body, userType } = await req.json();
    // Determine the Supabase project URL. Prefer the `SUPABASE_URL` secret but
    // fall back to the request headers (`x-forwarded-host` / `host`) so the
    // function still works even when the secret is missing.
    const host = req.headers.get("x-forwarded-host") ?? req.headers.get("host") ?? "";
    const proto = req.headers.get("x-forwarded-proto") ?? "https";
    const supabaseUrl = Deno.env.get("SUPABASE_URL") || (host ? `${proto}://${host}` : "");
    if (!supabaseUrl) throw new Error("SUPABASE_URL is required");
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    // Be flexible with env names to reduce setup friction
    const projectId =
      Deno.env.get("Firebase_Project_id") ||
      Deno.env.get("FIREBASE_PROJECT_ID") ||
      Deno.env.get("firebase_project_id") ||
      "";
    const serviceAccountJson =
      Deno.env.get("Service_Acount_Json") || // original (typo kept for backward compat)
      Deno.env.get("SERVICE_ACCOUNT_JSON") ||
      Deno.env.get("GOOGLE_SERVICE_ACCOUNT_JSON") ||
      "{}";
    const serviceAccount = JSON.parse(serviceAccountJson);
    const supabase = createClient(supabaseUrl, serviceRoleKey);
    const { data: tokens, error } = await supabase.from("device_tokens").select("token").eq("user_id", userId);
    if (error) {
      throw error;
    }
    // Validate Firebase credentials presence for clearer errors
    const missing: string[] = [];
    if (!projectId) missing.push("Firebase_Project_id/FIREBASE_PROJECT_ID");
    if (!serviceAccount?.client_email) missing.push("client_email in Service_Account_JSON");
    if (!serviceAccount?.private_key) missing.push("private_key in Service_Account_JSON");
    if (missing.length) {
      return new Response(JSON.stringify({
        error: `Firebase configuration is incomplete: missing ${missing.join(", ")}`
      }), { status: 400, headers: { "Content-Type": "application/json" } });
    }
    const auth = new GoogleAuth({
      credentials: serviceAccount,
      scopes: [
        "https://www.googleapis.com/auth/firebase.messaging"
      ]
    });
    const client = await auth.getClient();
    const accessToken = await client.getAccessToken();
    const results: Array<{ token: string; ok: boolean; status: number; body?: unknown }> = [];
    await Promise.all(tokens?.map(async ({ token }) => {
      const message: Record<string, unknown> = {
        token,
        notification: { title, body },
        android: {
          priority: "HIGH",
          notification: {
            channel_id: "rahmatmas_push"
          }
        }
      };
      if (userType) {
        message.data = { userType };
      }
      const res = await fetch(`https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`
        },
        body: JSON.stringify({ message })
      });
      let json: unknown = undefined;
      try { json = await res.json(); } catch (_) {}
      results.push({ token, ok: res.ok, status: res.status, body: json });
    }) ?? []);
    const failures = results.filter(r => !r.ok);
    return new Response(JSON.stringify({
      success: failures.length === 0,
      sent: results.length,
      failures
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
