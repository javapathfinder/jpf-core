# Java PathFinder

An extensible software model checking framework for Java bytecode programs

## General Information about JPF

All the latest developments, changes, documentation can be found on our
[wiki](https://github.com/javapathfinder/jpf-core/wiki) page.

## Building and Installing

If you are having problems installing and running JPF, please look at the [How
to install
JPF](https://github.com/javapathfinder/jpf-core/wiki/How-to-install-JPF) guide.

Note that we have transitioned to OpenJDK with the new version of JPF (for Java 11), so unlike branch `java-8`, the new default version for Java 11 no longer compiles using Oracle's JDK.

We have documented on the wiki a lot of common problems during the install and
build processes reported by users.  If you are facing any issue, please, make
sure that we have not addressed it in documentation. Otherwise, feel free to
join our [Discord server](https://discord.gg/sX4YZUVHK7) or open an issue on the Issue
Tracker.

## Documentation

There is a constant effort to update and add JPF documentation on the wiki.
If you would like to contribute in that, please join our [Discord server](https://discord.gg/sX4YZUVHK7).

Contributions are welcomed and we encourage you to get involved with the
community.

Perfect — here’s the **official setup command sequence** you can give to your contributors so they can easily install and run your Jekyll docs site on their system 👇

---

###  **For Contributors — Setup Instructions**

#### **1. Install Ruby and Bundler**

(If not already installed)

**macOS (Homebrew):**

```bash
brew install ruby
echo 'export PATH="/opt/homebrew/opt/ruby/bin:$PATH"' >> ~/.zshrc
exec zsh
gem install bundler
```

**Ubuntu/Linux:**

```bash
sudo apt install ruby-full build-essential zlib1g-dev
gem install bundler
```

---

#### **2. Clone the repository**

```bash
git clone https://github.com/<your-username>/<repo-name>.git
cd <repo-name>/docs
```

---

#### **3. Install dependencies**

```bash
bundle install
```

*(If they see permission errors, they can run:)*

```bash
bundle install --path vendor/bundle
```

---

#### **4. Run the local server**

```bash
bundle exec jekyll serve
```

Then open the site at:
👉 **[http://127.0.0.1:4000/jpf-core/](http://127.0.0.1:4000/jpf-core/)**

---

#### **5. (Optional) Stop the server**

Press `Ctrl + C` in the terminal.

---


Happy Verification
*-- the Java PathFinder team*


---

## Running with Docker

You can build and run JPF inside a container using the provided `Dockerfile` and `docker-compose.yml`.

### 1. Build the image
```bash
docker-compose build
```
### 2. Start a container
```bash
docker-compose run --rm jpf-dev
```
### 3. Build JPF
```bash
./gradlew clean build
```