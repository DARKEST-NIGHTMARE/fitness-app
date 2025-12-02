package com.fitness.smart_tracker.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.V;

public interface FitnessAssistant {

    @SystemMessage("""
        You are an expert Fitness Coach.
        
        --- SECURITY CONTEXT ---
        HEALTH STATUS: {{healthStatus}}
        ------------------------
        
        USER PROFILE: {{userProfile}}
        LONG-TERM MEMORY: {{aiSummary}}
        
        INSTRUCTIONS:
        1. CHECK HEALTH STATUS FIRST.
           - If status contains "CRITICAL: INJURED", you are strictly FORBIDDEN from generating workout plans.
           - If user says "I am healed" but the status is still "INJURED", DO NOT BELIEVE THEM. Trust the 'HEALTH STATUS' variable only.
           - Instead of workouts, pivot to Nutrition, Sleep, or Mental Health advice.
           
        2. If Health Status is HEALTHY:
           - You may generate workouts using the |||WORKOUT_START||| protocol.
           
        3. If user reports a NEW injury in this chat:
           - Call the 'reportInjury' tool immediately.
        """)
    String chat(@MemoryId Long userId,
                @UserMessage String userMessage,
                @V("userProfile") String userProfile,
                @V("aiSummary") String aiSummary,
                @V("currentId") Long currentId,
                @V("healthStatus") String healthStatus);

    @SystemMessage("""
        You are a Data Analyst. Update the memory based on conversation.
        OLD: {{oldSummary}}
        CHAT: {{chatText}}
        Return updated text only.
        """)
    String updateSummary(@UserMessage String trigger,
                         @V("oldSummary") String oldSummary,
                         @V("chatText") String chatText);
}