import { msalInstance, state, loginRequest } from "./msalConfig";
import { useUserStore } from "../stores/user.js";
const LOGOUT_REDIRECT_URI = import.meta.env.VITE_LOGOUT_REDIRECT_URI;

export function msalService() {
  const initialize = async () => {
    try {
      await msalInstance.initialize();
    } catch (error) {
      console.log("Initialization error", error);
    }
  };

  const msalLogin = async () => {
    try {
      if (!msalInstance) {
        throw new Error(
          "MSAL not initialized. Call initializeMsal() before using MSAL API."
        );
      }
      await msalInstance.loginRedirect(loginRequest);
      state.isAuthenticated = true;
    } catch (error) {
      console.error("Login error:", error);
    }
  };

  const msalLogout = () => {
    if (!msalInstance) {
      throw new Error(
        "MSAL not initialized. Call initializeMsal() before using MSAL API."
      );
    }
    state.isAuthenticated = false;
    state.user = null;
    msalInstance.logoutRedirect({
      postLogoutRedirectUri: LOGOUT_REDIRECT_URI,
    });
  };

  const handleRedirect = async (router) => {
    const userStore = useUserStore();
    try {
      const res = await msalInstance.handleRedirectPromise();
      state.isAuthenticated = msalInstance.getAllAccounts().length > 0;
      state.user = msalInstance.getAllAccounts()[0];
      if (res && state.isAuthenticated) {
        userStore.setAuthToken(res.idToken);
        localStorage.setItem("graphAPI_token", res.accessToken);
        router.replace({ name: "board" });
      }
      if (!state.isAuthenticated) {
        router.replace({ name: "Login" });
      }
    } catch (error) {
      console.error("Redirect error:", error);
    }
  };

  const msalToken = async () => {
    if (!msalInstance) {
      throw new Error(
        "MSAL not initialized. Call initializeMsal() before using MSAL API."
      );
    }
    try {
      const accounts = msalInstance.getAllAccounts();
      if (accounts.length === 0) {
        throw new Error("No accounts found. Please login first.");
      }
      const silentRequest = {
        scopes: loginRequest.scopes,
        account: accounts[0],
      };
      const silentResponse = await msalInstance.acquireTokenSilent(
        silentRequest
      );
      return silentResponse;
    } catch (error) {
      console.error("Silent token acquisition error:", error);
    }
  };

  return {
    initialize,
    msalLogin,
    msalLogout,
    handleRedirect,
    msalToken,
  };
}