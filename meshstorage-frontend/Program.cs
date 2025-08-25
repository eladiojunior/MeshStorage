using meshstorage_frontend.Helper;
using meshstorage_frontend.Services;
using meshstorage_frontend.Services.Cache;
using meshstorage_frontend.Settings;
using Microsoft.Extensions.Caching.Memory;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllersWithViews();
builder.Services.AddHttpClient<IApiService, ApiService>();
builder.Services.Configure<ApiSettings>(builder.Configuration.GetSection("ApiSettings"));
builder.Services.AddScoped<RazorViewToStringRenderer>();
builder.Services.AddMemoryCache();
builder.Services.AddSingleton<ICacheHelper, CacheHelper>();
builder.Services.AddSingleton<MapperHelper>();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{idApplication?}");

app.Run();