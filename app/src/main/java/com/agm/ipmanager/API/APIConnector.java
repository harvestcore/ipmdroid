package com.agm.ipmanager.API;

import android.content.Context;

import com.agm.ipmanager.IPManager;
import com.agm.ipmanager.Service;
import com.agm.ipmanager.events.Event;
import com.agm.ipmanager.events.EventType;
import com.agm.ipmanager.machines.Machine;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class APIConnector {
    private RequestQueue requestQueue;
    private Context context;

    public APIConnector() {}

    public void setContext(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void login() {
        if (IPManager.getInstance().hasCredentials()) {
            String url = IPManager.getInstance().getCredentials().hostname + "/api/login";

            CustomRequest request = new CustomRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String token = response.getString("token");

                        if (!token.isEmpty()) {
                            IPManager.getInstance().setToken(token);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            requestQueue.add(request);
        }
    }

    public HashMap<Service, Boolean> getServiceStatus() {
        final HashMap<Service, Boolean> output = new HashMap<>();
        if (IPManager.getInstance().hasCredentials()) {
            String url = IPManager.getInstance().getCredentials().hostname + "/api/status";

            CustomRequest request = new CustomRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject mongo = response.getJSONObject("mongo");
                        JSONObject docker = response.getJSONObject("docker");

                        output.put(Service.MONGO, mongo.getBoolean("is_up"));
                        output.put(Service.DOCKER, docker.getBoolean("is_up"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            requestQueue.add(request);
        }

        return output;
    }

    public void setMachines() {
        final ArrayList<Machine> output = new ArrayList<>();
        if (IPManager.getInstance().hasCredentials()) {
            String url = IPManager.getInstance().getCredentials().hostname + "/api/machine/query";
            JSONObject query = new JSONObject();
            try {
                query.put("query", new JSONObject());
                query.put("filter", new JSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomRequest request = new CustomRequest(Request.Method.POST, url, query, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        try {
                            String ipv6 = "";
                            String mac = "";
                            try {
                                ipv6 = response.getString("ipv6");
                                mac = response.getString("mac");
                            } catch (JSONException ee) {}

                            output.add(new Machine(
                                response.getString("name"),
                                response.getString("type"),
                                response.getString("ipv4"),
                                ipv6,
                                mac
                            ));
                        } catch (JSONException e) {
                            JSONArray items = response.getJSONArray("items");
                            int total = response.getInt("total");

                            for (int i = 0; i < total; ++i) {
                                JSONObject aux = items.getJSONObject(i);
                                String ipv6 = "";
                                String mac = "";
                                try {
                                    ipv6 = aux.getString("ipv6");
                                    mac = aux.getString("mac");
                                } catch (JSONException ee) {}

                                output.add(new Machine(
                                        aux.getString("name"),
                                        aux.getString("type"),
                                        aux.getString("ipv4"),
                                        ipv6,
                                        mac
                                ));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    IPManager.getInstance().setMachines(output);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            requestQueue.add(request);
        }
    }

    public void addMachine(Machine m) {
        if (IPManager.getInstance().hasCredentials()) {
            String url = IPManager.getInstance().getCredentials().hostname + "/api/machine";
            JSONObject query = new JSONObject();
            try {
                query.put("name", m.name);
                query.put("type", m.type);
                query.put("ipv4", m.ipv4);

                if (!m.ipv6.isEmpty()) {
                    query.put("ipv6", m.ipv6);
                }
                if (!m.mac.isEmpty()) {
                    query.put("mac", m.mac);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomRequest request = new CustomRequest(Request.Method.POST, url, query, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        boolean status = response.getBoolean("ok");

                        if (status) {
                            setMachines();
                            IPManager.getInstance().addEvent(new Event(EventType.MACHINE, "Machine added"));
                        } else {
                            IPManager.getInstance().addEvent(new Event(EventType.MACHINE, "Machine not added"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            requestQueue.add(request);
        }
    }

    public void removeMachine(Machine m) {
        if (IPManager.getInstance().hasCredentials()) {
            String url = IPManager.getInstance().getCredentials().hostname + "/api/machine";
            JSONObject query = new JSONObject();
            try {
                query.put("name", m.name);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomRequest request = new CustomRequest(Request.Method.DELETE, url, query, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        boolean status = response.getBoolean("ok");

                        if (status) {
                            setMachines();
                            IPManager.getInstance().addEvent(new Event(EventType.MACHINE, "Machine deleted"));
                        } else {
                            IPManager.getInstance().addEvent(new Event(EventType.MACHINE, "Machine not deleted"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            requestQueue.add(request);
        }
    }
}
