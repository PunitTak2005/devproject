package com.javaproject.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.javaproject.beans.BoardGame;
import com.javaproject.beans.Review;
import com.javaproject.database.DatabaseAccess;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private static final String REDIRECT_HOME = "redirect:/";
    private static final String REVIEWS_URL_SUFFIX = "/reviews";
    private static final String VIEW_NEW_USER = "new-user";
    private static final String VIEW_SECURED_ADD_REVIEW = "secured/addReview";
    private static final String MODEL_BOARDGAME = "boardgame";
    private static final String MODEL_REVIEW = "review";
    private static final String MODEL_AUTHORITIES = "authorities";
    private static final String LOG_RETURN_VALUE = "Return value is: {}";

    @Autowired
    DatabaseAccess da;

    @Autowired
    @Lazy
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JdbcUserDetailsManager jdbcUserDetailsManager;

    @GetMapping("/newUser")
    public String newUser(Model model) {

        List<String> authorities = da.getAuthorities();
        model.addAttribute(MODEL_AUTHORITIES, authorities);
        return VIEW_NEW_USER;
    }

    @PostMapping("/addUser")
    public String addUser(@RequestParam String userName, @RequestParam String password,
            @RequestParam String[] authorities, Model model, RedirectAttributes redirectAttrs) {

        List<GrantedAuthority> authorityList = new ArrayList<>();

        for (String authority : authorities) {
            authorityList.add(new SimpleGrantedAuthority(authority));
        }
        String encodedPassword = passwordEncoder.encode(password);

        // check existing user
        if (jdbcUserDetailsManager.userExists(userName)) {
            model.addAttribute("errorMsg", "User name already Exists. Try a different user name.");
            model.addAttribute(MODEL_AUTHORITIES, authorityList);
            return VIEW_NEW_USER;
        } else {
            User user = new User(userName, encodedPassword, authorityList);

            jdbcUserDetailsManager.createUser(user);
            redirectAttrs.addFlashAttribute("userAddedMsg", "User succesfully added!");
            return REDIRECT_HOME;
        }
    }

    @GetMapping("/")
    public String goHome(Model model) {
        List<BoardGame> boardgames = da.getBoardGames();
        model.addAttribute("boardgames", boardgames);
        return "index";
    }

    @GetMapping("/{id}")
    public String getBoardgameDetail(@PathVariable Long id, Model model) {
        model.addAttribute(MODEL_BOARDGAME, da.getBoardGame(id));
        return MODEL_BOARDGAME;
    }

    @GetMapping("/{id}/reviews")
    public String getReviews(@PathVariable Long id, Model model) {
        model.addAttribute(MODEL_BOARDGAME, da.getBoardGame(id));
        model.addAttribute("reviews", da.getReviews(id));
        return MODEL_REVIEW;
    }

    @GetMapping("/secured/addReview/{id}")
    public String addReview(@PathVariable Long id, Model model) {
        model.addAttribute(MODEL_BOARDGAME, da.getBoardGame(id));
        model.addAttribute(MODEL_REVIEW, new Review());

        return VIEW_SECURED_ADD_REVIEW;
    }

    // edit the review
    @GetMapping("/{gameId}/reviews/{id}")
    public String editReview(@PathVariable Long gameId, @PathVariable Long id, Model model) {
        Review review = da.getReview(id);
        model.addAttribute(MODEL_REVIEW, review);
        model.addAttribute(MODEL_BOARDGAME, da.getBoardGame(gameId));
        return VIEW_SECURED_ADD_REVIEW;
    }

    @GetMapping("/secured/addBoardGame")
    public String addBoardGame(Model model) {
        model.addAttribute(MODEL_BOARDGAME, new BoardGame());
        return "secured/addBoardGame";
    }

    @PostMapping("/boardgameAdded")
    public String boardgameAdded(@ModelAttribute BoardGame boardgame) {
        Long returnValue = da.addBoardGame(boardgame);
        logger.info(LOG_RETURN_VALUE, returnValue);
        return REDIRECT_HOME;
    }

    @PostMapping("/reviewAdded")
    public String reviewAdded(@ModelAttribute Review review) {
        int returnValue;
        // if id exists, edit
        if (review.getId() != null) {
            returnValue = da.editReview(review);
        } else {
            // if id not exists, add
            returnValue = da.addReview(review);
        }
        logger.info(LOG_RETURN_VALUE, returnValue);
        return REDIRECT_HOME + review.getGameId() +
                REVIEWS_URL_SUFFIX;
    }

    @GetMapping("/deleteReview/{id}")
    public String deleteReview(@PathVariable Long id) {
        Long gameId = da.getReview(id).getGameId();
        int returnValue = da.deleteReview(id);
        logger.info(LOG_RETURN_VALUE, returnValue);
        return REDIRECT_HOME + gameId + REVIEWS_URL_SUFFIX;
    }

    @GetMapping("/user")
    public String goToUserSecured() {
        return "secured/user/index";
    }

    @GetMapping("/manager")
    public String goToManagerSecured() {
        return "secured/manager/index";
    }

    @GetMapping("/secured")
    public String goToSecured() {
        return "secured/gateway";
    }

    @GetMapping("/login")
    public String goToLogin() {
        return "login";
    }

    @GetMapping("/permission-denied")
    public String goToDenied() {
        return "error/permission-denied";
    }
}
