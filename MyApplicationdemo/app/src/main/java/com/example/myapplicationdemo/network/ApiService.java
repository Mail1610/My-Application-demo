package com.example.myapplicationdemo.network;

import com.example.myapplicationdemo.network.dto.AuthResponse;
import com.example.myapplicationdemo.network.dto.ExpenseRequest;
import com.example.myapplicationdemo.network.dto.ExpenseResponse;
import com.example.myapplicationdemo.network.dto.GroupRequest;
import com.example.myapplicationdemo.network.dto.GroupResponse;
import com.example.myapplicationdemo.network.dto.LoginRequest;
import com.example.myapplicationdemo.network.dto.RegisterRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    // ── Groups ────────────────────────────────────────────────────
    @GET("api/groups")
    Call<List<GroupResponse>> getGroups();

    @POST("api/groups")
    Call<GroupResponse> createGroup(@Body GroupRequest request);

    @DELETE("api/groups/{id}")
    Call<Void> deleteGroup(@Path("id") int groupId);

    // ── Expenses ──────────────────────────────────────────────────
    @GET("api/groups/{groupId}/expenses")
    Call<List<ExpenseResponse>> getExpenses(@Path("groupId") int groupId);

    @POST("api/groups/{groupId}/expenses")
    Call<ExpenseResponse> createExpense(@Path("groupId") int groupId,
                                        @Body ExpenseRequest request);

    @DELETE("api/expenses/{id}")
    Call<Void> deleteExpense(@Path("id") int expenseId);
}
