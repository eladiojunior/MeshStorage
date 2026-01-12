FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
USER $APP_UID
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
# Copia o csproj corretamente
COPY meshstorage-frontend/meshstorage-frontend.csproj ./meshstorage-frontend/
WORKDIR /src/meshstorage-frontend
RUN dotnet restore "meshstorage-frontend.csproj"

# Copia o restante do projeto
COPY meshstorage-frontend/ ./

RUN dotnet build "meshstorage-frontend.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "meshstorage-frontend.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "meshstorage-frontend.dll"]